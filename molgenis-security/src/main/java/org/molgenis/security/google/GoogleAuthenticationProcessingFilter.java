package org.molgenis.security.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import org.molgenis.auth.*;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.token.UnknownTokenException;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisGroupMetaData.NAME;
import static org.molgenis.auth.MolgenisUserMetaData.*;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.springframework.http.HttpMethod.POST;

public class GoogleAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(GoogleAuthenticationProcessingFilter.class);

	public static final String GOOGLE_AUTHENTICATION_URL = "/login/google";
	static final String PARAM_ID_TOKEN = "id_token";
	private static final String PROFILE_KEY_GIVEN_NAME = "given_name";
	private static final String PROFILE_KEY_FAMILY_NAME = "family_name";

	private final GooglePublicKeysManager googlePublicKeysManager;
	private final DataService dataService;
	private final MolgenisUserDetailsService molgenisUserDetailsService;
	private final AppSettings appSettings;
	private final MolgenisUserFactory molgenisUserFactory;
	private final MolgenisGroupMemberFactory molgenisGroupMemberFactory;

	@Autowired
	public GoogleAuthenticationProcessingFilter(GooglePublicKeysManager googlePublicKeysManager,
			DataService dataService, MolgenisUserDetailsService molgenisUserDetailsService, AppSettings appSettings,
			MolgenisUserFactory molgenisUserFactory, MolgenisGroupMemberFactory molgenisGroupMemberFactory)
	{
		super(new AntPathRequestMatcher(GOOGLE_AUTHENTICATION_URL, POST.toString()));
		this.molgenisUserFactory = requireNonNull(molgenisUserFactory);
		this.molgenisGroupMemberFactory = requireNonNull(molgenisGroupMemberFactory);

		setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error"));

		this.googlePublicKeysManager = requireNonNull(googlePublicKeysManager);
		this.dataService = requireNonNull(dataService);
		this.molgenisUserDetailsService = requireNonNull(molgenisUserDetailsService);
		this.appSettings = requireNonNull(appSettings);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException
	{
		if (appSettings.getGoogleSignIn() == false)
		{
			throw new AuthenticationServiceException("Google authentication not available");
		}

		String idTokenString = request.getParameter(PARAM_ID_TOKEN);

		if (idTokenString != null)
		{
			// verify token string is valid
			GoogleIdToken idToken;
			try
			{
				idToken = verify(idTokenString);
			}
			catch (GeneralSecurityException e)
			{
				throw new UnknownTokenException(e.getMessage(), e);
			}

			// google token is null implies that verification failed
			if (idToken != null)
			{
				return createAuthentication(idToken.getPayload());
			}
			else
			{
				throw new BadCredentialsException(format("Token [%s] verification failed", idTokenString));
			}
		}
		throw new UnknownTokenException(idTokenString);
	}

	private GoogleIdToken verify(String idTokenString) throws GeneralSecurityException, IOException
	{
		List<String> audience = Collections.singletonList(appSettings.getGoogleAppClientId());
		GoogleIdTokenVerifier googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(googlePublicKeysManager)
				.setAudience(audience).build();
		return googleIdTokenVerifier.verify(idTokenString);
	}

	private Authentication createAuthentication(Payload payload)
	{
		String email = payload.getEmail();
		if (email == null)
		{
			throw new AuthenticationServiceException(
					"Google ID token is missing required [email] claim, did you forget to specify scope [email]?");
		}
		Boolean emailVerified = payload.getEmailVerified();
		if (emailVerified != null && emailVerified.booleanValue() == false)
		{
			throw new AuthenticationServiceException("Google account email is not verified");
		}
		String principal = payload.getSubject();
		String credentials = payload.getAccessTokenHash();

		return runAsSystem(() ->
		{
			MolgenisUser user;

			user = dataService.query(MOLGENIS_USER, MolgenisUser.class).eq(GOOGLEACCOUNTID, principal).findOne();
			if (user == null)
			{
				// no user with google account
				user = dataService.query(MOLGENIS_USER, MolgenisUser.class).eq(EMAIL, email).findOne();
				if (user != null)
				{
					// connect google account to user
					user.setGoogleAccountId(principal);
					dataService.update(MOLGENIS_USER, user);
				}
				else
				{
					// create new user
					String username = email;
					String givenName = payload.containsKey(PROFILE_KEY_GIVEN_NAME) ? payload.get(PROFILE_KEY_GIVEN_NAME)
							.toString() : null;
					String familyName = payload.containsKey(PROFILE_KEY_FAMILY_NAME) ? payload
							.get(PROFILE_KEY_FAMILY_NAME).toString() : null;
					user = createMolgenisUser(username, email, givenName, familyName, principal);
				}
			}
			if (!user.isActive())
			{
				throw new DisabledException(MolgenisLoginController.ERROR_MESSAGE_DISABLED);
			}
			// create authentication
			Collection<? extends GrantedAuthority> authorities = molgenisUserDetailsService.getAuthorities(user);
			return new UsernamePasswordAuthenticationToken(user.getUsername(), credentials, authorities);
		});
	}

	private MolgenisUser createMolgenisUser(String username, String email, String givenName, String familyName,
			String googleAccountId)
	{
		if (!appSettings.getSignUp())
		{
			throw new AuthenticationServiceException("Google authentication not possible: sign up disabled");
		}

		if (appSettings.getSignUpModeration())
		{
			throw new AuthenticationServiceException("Google authentication not possible: sign up moderation enabled");
		}

		// create user
		LOG.info("first login for [{}], creating MOLGENIS user", username);
		MolgenisUser user = molgenisUserFactory.create();
		user.setUsername(username);
		user.setPassword(UUID.randomUUID().toString());
		user.setEmail(email);
		user.setActive(true);
		user.setSuperuser(false);
		user.setChangePassword(false);
		if (givenName != null)
		{
			user.setFirstName(givenName);
		}
		if (familyName != null)
		{
			user.setLastName(familyName);
		}
		user.setGoogleAccountId(googleAccountId);
		dataService.add(MOLGENIS_USER, user);

		// add user to all-users group
		MolgenisGroupMember groupMember = molgenisGroupMemberFactory.create();
		MolgenisGroup group = dataService.query(MOLGENIS_GROUP, MolgenisGroup.class).eq(NAME, ALL_USER_GROUP).findOne();
		groupMember.setMolgenisGroup(group);
		groupMember.setMolgenisUser(user);
		dataService.add(MOLGENIS_GROUP_MEMBER, groupMember);

		return user;
	}
}

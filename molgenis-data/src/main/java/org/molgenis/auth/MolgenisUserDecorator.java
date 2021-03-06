package org.molgenis.auth;

import com.google.common.collect.Iterators;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.AuthorityMetaData.ROLE;
import static org.molgenis.auth.UserAuthorityMetaData.MOLGENIS_USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;

public class MolgenisUserDecorator implements Repository<MolgenisUser>
{
	private static final int BATCH_SIZE = 1000;

	private final Repository<MolgenisUser> decoratedRepository;
	private final UserAuthorityFactory userAuthorityFactory;
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public MolgenisUserDecorator(Repository<MolgenisUser> decoratedRepository,
			UserAuthorityFactory userAuthorityFactory, DataService dataService, PasswordEncoder passwordEncoder)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public void add(MolgenisUser entity)
	{
		encodePassword(entity);
		decoratedRepository.add(entity);
		addSuperuserAuthority(entity);
	}

	@Override
	public void update(MolgenisUser entity)
	{
		updatePassword(entity);
		decoratedRepository.update(entity);
		updateSuperuserAuthority(entity);
	}

	@Override
	public Integer add(Stream<MolgenisUser> entities)
	{
		AtomicInteger count = new AtomicInteger();
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(molgenisUsers ->
		{
			molgenisUsers.forEach(this::encodePassword);

			Integer batchCount = decoratedRepository.add(molgenisUsers.stream());
			count.addAndGet(batchCount);

			molgenisUsers.forEach(this::addSuperuserAuthority);
		});
		return count.get();
	}

	@Override
	public void update(Stream<MolgenisUser> entities)
	{
		entities = entities.map(entity ->
		{
			updatePassword(entity);
			return entity;
		});
		decoratedRepository.update(entities);
	}

	private void updatePassword(MolgenisUser molgenisUser)
	{
		MolgenisUser currentUser = findOneById(molgenisUser.getId());

		String currentPassword = currentUser.getPassword();
		String password = molgenisUser.getPassword();
		//password is updated
		if(!currentPassword.equals(password))
		{
			password = passwordEncoder.encode(molgenisUser.getPassword());
		}
		molgenisUser.setPassword(password);
	}

	private void encodePassword(MolgenisUser molgenisUser)
	{
		String password = molgenisUser.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		molgenisUser.setPassword(encodedPassword);
	}

	private void addSuperuserAuthority(MolgenisUser molgenisUser)
	{
		Boolean isSuperuser = molgenisUser.isSuperuser();
		if (isSuperuser != null && isSuperuser)
		{
			UserAuthority userAuthority = userAuthorityFactory.create();
			userAuthority.setMolgenisUser(molgenisUser);
			userAuthority.setRole(AUTHORITY_SU);

			getUserAuthorityRepository().add(userAuthority);
		}
	}

	private void updateSuperuserAuthority(MolgenisUser molgenisUser)
	{
		Repository<UserAuthority> userAuthorityRepo = getUserAuthorityRepository();
		UserAuthority suAuthority = userAuthorityRepo.query().eq(MOLGENIS_USER, molgenisUser).and()
				.eq(ROLE, AUTHORITY_SU).findOne();

		Boolean isSuperuser = molgenisUser.isSuperuser();
		if (isSuperuser != null && isSuperuser)
		{
			if (suAuthority == null)
			{
				UserAuthority userAuthority = userAuthorityFactory.create();
				userAuthority.setMolgenisUser(molgenisUser);
				userAuthority.setRole(AUTHORITY_SU);
				userAuthorityRepo.add(userAuthority);
			}
		}
		else
		{
			if (suAuthority != null)
			{
				userAuthorityRepo.deleteById(suAuthority.getId());
			}
		}
	}

	private Repository<UserAuthority> getUserAuthorityRepository()
	{
		return dataService.getRepository(USER_AUTHORITY, UserAuthority.class);
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public Iterator<MolgenisUser> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<MolgenisUser>> consumer, int batchSize)
	{
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query<MolgenisUser> query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query<MolgenisUser> q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<MolgenisUser> findAll(Query<MolgenisUser> q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public MolgenisUser findOne(Query<MolgenisUser> q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public MolgenisUser findOneById(Object id)
	{
		return decoratedRepository.findOneById(id);
	}

	@Override
	public MolgenisUser findOneById(Object id, Fetch fetch)
	{
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<MolgenisUser> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<MolgenisUser> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public void delete(MolgenisUser entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<MolgenisUser> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decoratedRepository.getQueryOperators();
	}
}

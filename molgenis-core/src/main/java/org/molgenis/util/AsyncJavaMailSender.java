package org.molgenis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;

import javax.mail.internet.MimeMessage;

/**
 * Asynchronous mail sender
 */
public class AsyncJavaMailSender extends JavaMailSenderImpl
{
	private static final Logger LOG = LoggerFactory.getLogger(AsyncJavaMailSender.class);

	@Override
	@Async
	public void send(MimeMessage mimeMessage) throws MailException
	{
		super.send(mimeMessage);
	}

	@Override
	@Async
	public void send(MimeMessage[] mimeMessages) throws MailException
	{
		super.send(mimeMessages);
	}

	@Override
	@Async
	public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException
	{
		super.send(mimeMessagePreparator);
	}

	@Override
	@Async
	public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException
	{
		super.send(mimeMessagePreparators);
	}

	@Override
	@Async
	public void send(SimpleMailMessage simpleMessage) throws MailException
	{
		try
		{
			super.send(simpleMessage);
		}
		catch (Exception e)
		{
			LOG.error("Error sending e-mail.", e);
		}
	}

	@Override
	@Async
	public void send(SimpleMailMessage[] simpleMessages) throws MailException
	{
		super.send(simpleMessages);
	}
}
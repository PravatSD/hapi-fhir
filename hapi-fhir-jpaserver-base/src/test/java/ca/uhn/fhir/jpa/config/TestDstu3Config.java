package ca.uhn.fhir.jpa.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
@EnableTransactionManagement()
public class TestDstu3Config extends BaseJavaConfigDstu3 {

	static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TestDstu3Config.class);

	@Bean()
	public DaoConfig daoConfig() {
		return new DaoConfig();
	}

	private boolean myLogConnection = false;

	@Bean()
	public DataSource dataSource() {
		BasicDataSource retVal = new BasicDataSource() {

			@Override
			public Connection getConnection() throws SQLException {
				if (myLogConnection) {
					logGetConnectionStackTrace();
					return new ConnectionWrapper(super.getConnection());
				} else {
					return super.getConnection();
				}
			}

			private void logGetConnectionStackTrace() {
				try {
					throw new Exception();
				} catch (Exception e) {
					StringBuilder b = new StringBuilder();
					b.append("New connection request:");
					for (StackTraceElement next : e.getStackTrace()) {
						if (next.getClassName().contains("fhir")) {
							b.append("\n   ").append(next.getClassName()).append(" ").append(next.getFileName()).append(":").append(next.getLineNumber());
						}
					}
					ourLog.info(b.toString());
				}
			}

		};
		retVal.setDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		retVal.setUrl("jdbc:derby:memory:myUnitTestDB;create=true");
		retVal.setUsername("");
		retVal.setPassword("");

		/*
		 * We use a randomized number of maximum threads in order to try
		 * and catch any potential deadlocks caused by database connection
		 * starvation
		 */
		int maxThreads = (int) (Math.random() * 6) + 1;
		retVal.setMaxTotal(maxThreads);

		DataSource dataSource = ProxyDataSourceBuilder
				.create(retVal)
				// .logQueryBySlf4j(SLF4JLogLevel.INFO, "SQL")
				.logSlowQueryBySlf4j(10, TimeUnit.SECONDS)
				.countQuery()
				.build();

		return dataSource;
	}

	@Bean()
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean retVal = new LocalContainerEntityManagerFactoryBean();
		retVal.setPersistenceUnitName("PU_HapiFhirJpaDstu3");
		retVal.setDataSource(dataSource());
		retVal.setPackagesToScan("ca.uhn.fhir.jpa.entity");
		retVal.setPersistenceProvider(new HibernatePersistenceProvider());
		retVal.setJpaProperties(jpaProperties());
		return retVal;
	}

	private Properties jpaProperties() {
		Properties extraProperties = new Properties();
		extraProperties.put("hibernate.jdbc.batch_size", "50");
		extraProperties.put("hibernate.format_sql", "false");
		extraProperties.put("hibernate.show_sql", "false");
		extraProperties.put("hibernate.hbm2ddl.auto", "update");
		extraProperties.put("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");
		extraProperties.put("hibernate.search.default.directory_provider", "ram");
		extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
		extraProperties.put("hibernate.search.autoregister_listeners", "true");
		return extraProperties;
	}

	/**
	 * Bean which validates incoming requests
	 */
	@Bean
	@Lazy
	public RequestValidatingInterceptor requestValidatingInterceptor() {
		RequestValidatingInterceptor requestValidator = new RequestValidatingInterceptor();
		requestValidator.setFailOnSeverity(ResultSeverityEnum.ERROR);
		requestValidator.setAddResponseHeaderOnSeverity(null);
		requestValidator.setAddResponseOutcomeHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
		requestValidator.addValidatorModule(instanceValidatorDstu3());

		return requestValidator;
	}

	@Bean()
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}

}

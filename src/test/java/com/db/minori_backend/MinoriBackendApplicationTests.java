
package com.db.minori_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MinoriBackendApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() {
	}

	@Test
	void deveUtilizarBancoDeDadosDeTeste() throws SQLException {

		try (Connection connection = dataSource.getConnection()) {

			assertThat(connection.getCatalog())
					.isEqualTo("minori_test");

			assertThat(connection.getMetaData().getUserName())
					.isEqualTo("minori_test_app");
		}
	}
}

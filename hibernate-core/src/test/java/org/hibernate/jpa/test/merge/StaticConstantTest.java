package org.hibernate.jpa.test.merge;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;

import org.hibernate.testing.TestForIssue;
import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertEquals;

/**
 * This class tests if merging a {@link Customer} that sets a default (static constant) {@link Embeddable}
 * {@link Address} in its default constructor changes the default value, which should not happen.
 * It also tests if the addresses of two customers that are merged subsequently (within the same transaction)
 * keep their original addresses as it should be.
 * As of writing this class neither of the two tests succeeds, see JIRA issue HHH-11885.
 *
 * @author Andreas Barth
 */
@TestForIssue(jiraKey = "HHH-11885")
public class StaticConstantTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { Customer.class };
	}

	@Test
	public void testMergingCustomerDoesNotChangeStaticConstantAddress() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.merge( new Customer( new Address( "paris" ) ) );
			assertEquals( "london", Address.DEFAULT.city );
		} );
	}

	@Test
	public void testMergingTwoCustomers() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			Customer parisCustomer = entityManager.merge( new Customer( new Address( "paris" ) ) );
			Customer berlinCustomer = entityManager.merge( new Customer( new Address( "berlin" ) ) );
			assertEquals( "paris", parisCustomer.getAddress().getCity() );
			assertEquals( "berlin", berlinCustomer.getAddress().getCity() );
			assertEquals( "london", Address.DEFAULT.city);
		} );
	}

	@Entity
	private static class Customer {
		@Id
		@GeneratedValue
		private Integer id;
		private Address address;

		Customer() {
			this.address = Address.DEFAULT;
		}

		public Customer(Address address) {
			this.address = address;
		}

		public Address getAddress() {
			return this.address;
		}

		@Override
		public String toString() {
			return "Customer{" + "id=" + id +
					", address=" + address +
					'}';
		}
	}

	@Embeddable
	public static class Address {
		static final Address DEFAULT = new Address( "london" );

		private String city;

		public Address(String city) {
			this.city = city;
		}

		Address() {
		}

		public String getCity() {
			return this.city;
		}

		@Override
		public String toString() {
			return "Address{" + "city='" + city + '\'' +
					'}';
		}
	}
}

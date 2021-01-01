package ru.iuriimudrak.restaurant.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "users_unique_email_idx")})
public class User extends AbstractNamedEntity {

	@Email
	@NotBlank
	@Size(max = 100)
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@NotBlank
	@Size(min = 5, max = 100)
	@Column(name = "password", nullable = false)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
	private boolean enabled = true;

	@NotNull
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(name = "registered", nullable = false, columnDefinition = "timestamp default now()")
	private Date registered = new Date();


	@Column(name = "role")
	@BatchSize(size = 200)
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
					uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role"}, name = "user_roles_unique_idx")})
	private Set<Role> roles;

	@JsonManagedReference
	@OrderBy("localDate DESC")
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private List<Vote> votes;

	public User(User u) {
		this(u.getId(), u.getName(), u.getEmail(), u.getPassword(), u.isEnabled(), u.getRegistered(), u.getRoles());
	}

	public User(Integer id, String name, String email, String password, Role role, Role... roles) {
		this(id, name, email, password, true, new Date(), EnumSet.of(role, roles));
	}

	public User(Integer id, String name, String email, String password, boolean enabled, Date registered, Collection<Role> roles) {
		super(id, name);
		this.email = email;
		this.password = password;
		this.enabled = enabled;
		this.registered = registered;
		setRoles(roles);
	}

	public void setRoles(Collection<Role> roles) {
		this.roles = CollectionUtils.isEmpty(roles) ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
	}
}

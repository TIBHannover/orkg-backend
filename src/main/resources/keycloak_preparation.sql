CREATE TABLE orkg_user(
id UUID PRIMARY KEY,
old_id UUID DEFAULT NULL,
keycloak_id UUID DEFAULT NULL,
organization_id UUID DEFAULT NULL,
observatory_id UUID DEFAULT NULL,
name varchar(255) DEFAULT NULL,
first_name varchar(255) DEFAULT NULL,
last_name varchar(255) DEFAULT NULL,
display_name varchar(255) DEFAULT NULL,
email varchar(255) DEFAULT NULL,
created timestamp default current_timestamp
);

CREATE TABLE observatory(
id UUID PRIMARY KEY,
observatory_name text DEFAULT NULL,
created timestamp default current_timestamp
);

CREATE TABLE organization(
id UUID PRIMARY KEY,
organization_name text DEFAULT NULL,
created timestamp default current_timestamp
);

CREATE TABLE organization_user_mapper(
id UUID PRIMARY KEY,
user_id UUID DEFAULT NULL,
organization_id UUID DEFAULT NULL,
created timestamp default current_timestamp
);

CREATE TABLE observatory_user_mapper(
id UUID PRIMARY KEY,
user_id UUID DEFAULT NULL,
observatory_id UUID DEFAULT NULL,
created timestamp default current_timestamp
);

INSERT INTO orkg_user(old_id, email, display_name) SELECT id, email, display_name from users;


CREATE EXTENSION IF NOT EXISTS LTREE;

CREATE TABLE research_fields_tree(
id uuid DEFAULT uuid_generate_v4(),
user_id uuid DEFAULT NULL,
research_field varchar(255),
path LTREE DEFAULT NULL,
research_field_name varchar(255) DEFAULT NULL,
created_date_time timestamp DEFAULT NULL
);

CREATE TABLE notification_updates(
id uuid DEFAULT uuid_generate_v4(),
research_field_tree_id uuid DEFAULT NULL,
user_id uuid DEFAULT NULL,
notification_by_user_id uuid DEFAULT NULL,
resource_id varchar(255) DEFAULT NULL,
resource_type varchar(255) DEFAULT NULL,
title varchar(255) DEFAULT NULL,
new_paper BOOLEAN DEFAULT FALSE,
created_date_time timestamp DEFAULT NULL
);

CREATE TABLE notification_email_settings(
id uuid DEFAULT uuid_generate_v4(),
user_id uuid DEFAULT NULL,
time_of_preference int DEFAULT -1
);


CREATE TABLE unsubscribed_resources(
id uuid DEFAULT uuid_generate_v4(),
user_id uuid DEFAULT NULL,
resource_id varchar(255) DEFAULT NULL
);


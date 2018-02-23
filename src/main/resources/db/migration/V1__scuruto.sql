CREATE TABLE articles
(
	article_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	title varchar(100) NOT NULL,
	body text NOT NULL,
	comments_count int DEFAULT 0 NOT NULL,
	stocks_count int DEFAULT 0 NOT NULL,
	likes_count int DEFAULT 0 NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	lock_version bigint DEFAULT 0,
	CONSTRAINT articles_pkey PRIMARY KEY (article_id)
);

CREATE TABLE articles_tags
(
	article_id bigint NOT NULL,
	tag_id bigint NOT NULL
);

CREATE TABLE comments
(
	comment_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	article_id bigint NOT NULL,
	body text NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT comments_pkey PRIMARY KEY (comment_id)
);

CREATE TABLE comment_histories
(
	comment_history_id bigserial NOT NULL,
	comment_id bigint NOT NULL,
	new_body text,
	old_body text,
	deleted boolean DEFAULT 'false' NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT comment_histories_pkey PRIMARY KEY (comment_history_id)
);

CREATE TABLE likes
(
	like_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	article_id bigint NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT likes_pkey PRIMARY KEY (like_id),
	UNIQUE (user_id, article_id)
);

CREATE TABLE notifications
(
	notification_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	article_id bigint NOT NULL,
	fragment_id bigint,
	sender_id bigint NOT NULL,
	type varchar(255) NOT NULL,
	state boolean DEFAULT 'false' NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT notifications_pkey PRIMARY KEY (notification_id)
);

CREATE TABLE servlet_sessions
(
	jsession_id varchar(32) NOT NULL,
	skinny_session_id bigint NOT NULL,
	created_at timestamp NOT NULL,
	CONSTRAINT servlet_sessions_pkey PRIMARY KEY (jsession_id)
);

CREATE TABLE skinny_sessions
(
	id bigserial NOT NULL,
	created_at timestamp NOT NULL,
	expire_at timestamp NOT NULL,
	CONSTRAINT skinny_sessions_pkey PRIMARY KEY (id)
);

CREATE TABLE skinny_session_attributes
(
	skinny_session_id bigint NOT NULL,
	attribute_name varchar(128) NOT NULL,
	attribute_value bytea,
	CONSTRAINT skinny_session_attributes_pkey PRIMARY KEY (skinny_session_id, attribute_name)
);

CREATE TABLE stocks
(
	stock_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	article_id bigint NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT stocks_pkey PRIMARY KEY (stock_id),
	UNIQUE (user_id, article_id)
);

CREATE TABLE tags
(
	tag_id bigserial NOT NULL,
	name varchar(255) NOT NULL UNIQUE,
	taggings_count int DEFAULT 0,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT tags_pkey PRIMARY KEY (tag_id)
);

CREATE TABLE update_histories
(
	update_history_id bigserial NOT NULL,
	article_id bigint NOT NULL,
	new_title varchar(255) NOT NULL,
	new_tags varchar(255),
	new_body text NOT NULL,
	old_title varchar(255) NOT NULL,
	old_tags varchar(255),
	old_body text NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT update_histories_pkey PRIMARY KEY (update_history_id)
);


CREATE TABLE uploads
(
	upload_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	original_filename varchar(255) NOT NULL,
	filename varchar(255) NOT NULL UNIQUE,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT uploads_pkey PRIMARY KEY (upload_id)
);


CREATE TABLE users
(
	user_id bigserial NOT NULL,
	name varchar(255) NOT NULL,
	email varchar(255) NOT NULL UNIQUE,
	password varchar(255),
	image_url text,
	comment text,
	locale varchar(2) NOT NULL,
	confirmation_token varchar(255),
	confirmation_sent_at timestamp,
	reset_password_token varchar(255),
	reset_password_sent_at timestamp,
	is_active boolean DEFAULT 'true' NOT NULL,
	created_at timestamp,
	updated_at timestamp,
	CONSTRAINT users_pkey PRIMARY KEY (user_id)
);

ALTER TABLE articles_tags
ADD FOREIGN KEY (article_id)
REFERENCES articles (article_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE comments
ADD FOREIGN KEY (article_id)
REFERENCES articles (article_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE likes
ADD FOREIGN KEY (article_id)
REFERENCES articles (article_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE notifications
ADD FOREIGN KEY (article_id)
REFERENCES articles (article_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE stocks
ADD FOREIGN KEY (article_id)
REFERENCES articles (article_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE update_histories
ADD FOREIGN KEY (article_id)
REFERENCES articles (article_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE servlet_sessions
ADD FOREIGN KEY (skinny_session_id)
REFERENCES skinny_sessions (id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE skinny_session_attributes
ADD FOREIGN KEY (skinny_session_id)
REFERENCES skinny_sessions (id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE articles_tags
ADD FOREIGN KEY (tag_id)
REFERENCES tags (tag_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE articles
ADD FOREIGN KEY (user_id)
REFERENCES users (user_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE comments
ADD FOREIGN KEY (user_id)
REFERENCES users (user_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE likes
ADD FOREIGN KEY (user_id)
REFERENCES users (user_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE notifications
ADD FOREIGN KEY (user_id)
REFERENCES users (user_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE stocks
ADD FOREIGN KEY (user_id)
REFERENCES users (user_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

ALTER TABLE uploads
ADD FOREIGN KEY (user_id)
REFERENCES users (user_id)
ON UPDATE RESTRICT
ON DELETE RESTRICT
;

CREATE INDEX index_articles_on_user_id ON articles (user_id);
CREATE INDEX articles_tags_article_id_fk ON articles_tags (article_id);
CREATE INDEX index_comments_on_article_id ON comments (article_id);
CREATE INDEX index_comments_on_user_id ON comments (user_id);
CREATE UNIQUE INDEX index_likes_on_user_id_and_article_id ON likes (user_id, article_id);
CREATE INDEX index_likes_on_article_id ON likes (article_id);
CREATE INDEX index_likes_on_user_id ON likes (user_id);
CREATE INDEX index_notifications_on_article_id ON notifications (article_id);
CREATE INDEX index_notifications_on_user_id ON notifications (user_id);
CREATE INDEX servlet_sessions_fk ON servlet_sessions (skinny_session_id);
CREATE UNIQUE INDEX index_stocks_on_user_id_and_article_id ON stocks (user_id, article_id);
CREATE INDEX index_stocks_on_article_id ON stocks (article_id);
CREATE INDEX index_stocks_on_user_id ON stocks (user_id);
CREATE INDEX index_update_histories_on_article_id ON update_histories (article_id);
CREATE INDEX index_uploads_on_user_id ON uploads (user_id);
CREATE UNIQUE INDEX index_users_on_email ON users (email, is_active);
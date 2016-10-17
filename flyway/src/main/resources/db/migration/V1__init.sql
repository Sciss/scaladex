create schema if not exists scaladex;

create table scaladex.Projects(
  id integer not null primary key,
  organization varchar(255) not null,
  repository varchar(255) not null,
  default_artifact varchar(255) not null,
  default_stable_version boolean not null,
  custom_scala_doc varchar(255),
  deprecated boolean not null,
  test boolean not null,
  contributors_wanted boolean not null,
  live_data boolean not null
);

create table scaladex.Keywords(
  id integer not null primary key,
  name varchar(1024) not null
);

create table scaladex.Projects_Keywords(
  project_id int not null,
  foreign key(project_id) references scaladex.Projects(id),
  keyword_id int not null,
  foreign key(keyword_id) references scaladex.Keywords(id),
  primary key(project_id, keyword_id)
);

create table scaladex.Projects_Github(
  project_id int not null,
  foreign key(project_id) references scaladex.Projects(id),
  readme varchar(1048576),
  description varchar(1024),
  homepage varchar(1024),
  logo varchar(1024),
  stars int,
  forks int,
  watchers int,
  issues int,
  commits int
);


create table scaladex.Contributors(
  id integer not null primary key,
  login varchar(255) not null,
  avatar varchar(255) not null,
  url varchar(255) not null
);

create table scaladex.Projects_Contributors(
  project_id int not null,
  foreign key(project_id) references scaladex.Projects(id),
  contributor_id int not null,
  foreign key(contributor_id) references scaladex.Contributors(id),
  primary key(project_id, contributor_id),
  commits int not null
);

create table scaladex.Projects_Artifacts(
  project_id integer not null references scaladex.Projects(id),
  name varchar(255)
)

-- create table scaladex.Releases(
--   id integer not null primary key,
--
--   project_id int not null,
--   foreign key(project_id) references scaladex.Projects(id),
--
--   groupId varchar(1024) not null,
--   artifactId varchar(1024) not null,
--   version varchar(1024) not null,
--
--   non_standard_lib boolean not null,
--   live_data boolean not null,
--   released date not null,
--   test boolean not null,
--   bintray_owner varchar(1024) not null,
--   bintray_repo varchar(1024) not null
-- );
--
-- create table scaladex.Licenses(
--   id integer not null primary key,
--   name varchar(1024) not null,
--   shortName varchar(1024) not null,
--   url varchar(1024)
-- );
--
-- create table scaladex.Releases_Licenses(
--   release_id int not null,
--   foreign key(release_id) references scaladex.Releases(id),
--   license_id int not null,
--   foreign key(license_id) references scaladex.Licenses(id),
--   primary key(release_id, license_id)
-- );
--
-- create table scaladex.Scala_Dependencies(
--   release1_id int not null,
--   foreign key(release1_id) references scaladex.Releases(id),
--   release2_id int not null,
--   foreign key(release2_id) references scaladex.Releases(id),
--   primary key(release1_id, release2_id),
--   scope varchar(255)
-- );
--
-- create table scaladex.Java_Releases(
--   id integer not null primary key,
--   groupId varchar(1024) not null,
--   artifactId varchar(1024) not null,
--   version varchar(1024) not null
-- );
--
-- create table scaladex.Java_Dependencies(
--   release_id integer not null,
--   foreign key(release_id) references scaladex.Releases(id),
--
--   java_release_id integer not null,
--   foreign key(java_release_id) references scaladex.Java_Releases(id),
--
--   primary key(release_id, java_release_id),
--   scope varchar(255)
-- );

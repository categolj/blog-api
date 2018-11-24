CREATE TABLE entry (
  entry_id           INT8         NOT NULL,
  title              VARCHAR(512) NOT NULL,
  content            TEXT         NOT NULL,
  created_by         VARCHAR(128),
  created_date       TIMESTAMP WITH TIME ZONE,
  last_modified_by   VARCHAR(128),
  last_modified_date TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (entry_id)
);


CREATE TABLE category (
  category_order INT2         NOT NULL,
  entry_id       INT8         NOT NULL,
  category_name  VARCHAR(128) NOT NULL,
  PRIMARY KEY (category_order, entry_id),
  FOREIGN KEY (entry_id) REFERENCES entry (entry_id)
  ON DELETE CASCADE
);

CREATE TABLE tag (
  tag_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (tag_name)
);

CREATE TABLE entry_tag (
  entry_id INT8         NOT NULL,
  tag_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (entry_id, tag_name),
  FOREIGN KEY (entry_id) REFERENCES entry (entry_id)
  ON DELETE CASCADE,
  FOREIGN KEY (tag_name) REFERENCES tag (tag_name)
  ON DELETE CASCADE
);

CREATE INDEX entry_last_modified_date
  ON entry (last_modified_date);

CREATE INDEX category_name_order
  ON category (category_name, category_order);
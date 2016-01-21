# Tasks schema

# --- !Ups

CREATE TABLE Tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    txt VARCHAR(255) NOT NULL,
    done BOOLEAN NOT NULL
);

INSERT INTO Tasks(txt, done) VALUES('Javascript, JQuery, AJAX', true);
INSERT INTO Tasks(txt, done) VALUES('Slick, SQL', false);
INSERT INTO Tasks(txt, done) VALUES('Scala, SBT', false);

# --- !Downs

DROP TABLE Tasks;
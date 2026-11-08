ALTER TABLE book ADD COLUMN subtitle       VARCHAR(500);
ALTER TABLE book ADD COLUMN original_title VARCHAR(500);
ALTER TABLE book ADD COLUMN edition        VARCHAR(255);
ALTER TABLE book ADD COLUMN pages          INTEGER;
ALTER TABLE book ADD COLUMN series         VARCHAR(255);
ALTER TABLE book ADD COLUMN summary        VARCHAR(4000);
ALTER TABLE book ADD COLUMN dewey_class    VARCHAR(50);
ALTER TABLE book ADD COLUMN sab_class      VARCHAR(50);

CREATE TABLE book_translator (
    book_id  BIGINT       NOT NULL,
    position INTEGER      NOT NULL,
    name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (book_id, position),
    CONSTRAINT fk_book_translator_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
);

CREATE TABLE book_genre (
    book_id  BIGINT       NOT NULL,
    position INTEGER      NOT NULL,
    name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (book_id, position),
    CONSTRAINT fk_book_genre_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
);

CREATE TABLE book_subject (
    book_id  BIGINT       NOT NULL,
    position INTEGER      NOT NULL,
    name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (book_id, position),
    CONSTRAINT fk_book_subject_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
);

CREATE INDEX idx_book_genre_name ON book_genre (name);

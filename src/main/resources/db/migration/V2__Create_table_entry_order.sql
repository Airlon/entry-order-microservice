CREATE TABLE IF NOT EXISTS PUBLIC.ENTRY_ORDER
(
	ID SERIAL NOT NULL,
	CODE VARCHAR(20) NOT NULL UNIQUE,
	TYPE VARCHAR(30) NOT NULL,
	CLASSIFICATION VARCHAR(30) NOT NULL,
	STATUS VARCHAR(30) NOT NULL,
    USER_CREATED_ID VARCHAR(100),
    USER_CREATED_EMAIL VARCHAR(200),
    DATE_CREATED_AT TIMESTAMP,
    USER_UPDATED_ID VARCHAR(100),
    USER_UPDATED_EMAIL VARCHAR(200),
    DATE_UPDATED_AT TIMESTAMP,
	CONSTRAINT ENTRY_ORDER_PKEY PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.ENTRY_ORDER_PRODUCT
(
	ID SERIAL NOT NULL,
	PRODUCT_ID BIGINT NOT NULL,
	TITLE VARCHAR(255),
	REGISTERED_USER_ID VARCHAR(100) NOT NULL,
	REGISTERED_USER_NAME VARCHAR(200) NOT NULL,
	REGISTERED_USER_EMAIL VARCHAR(255) NOT NULL,
	ENTRY_ORDER_PRODUCT_STATUS VARCHAR(50) NOT NULL,
	PRODUCT_ORIGIN VARCHAR(50) NOT NULL,
	SENT_RENOVA BOOLEAN DEFAULT FALSE,
	ENTRY_ORDER_ID BIGINT NOT NULL,
	REGISTRATION_DATE TIMESTAMP NOT NULL,
    CONSTRAINT FK_ENTRY_ORDER_PRODUCT_ENTRY FOREIGN KEY (ENTRY_ORDER_ID) REFERENCES ENTRY_ORDER(ID),
	CONSTRAINT ENTRY_ORDER_PRODUCT_PKEY PRIMARY KEY (ID)
);
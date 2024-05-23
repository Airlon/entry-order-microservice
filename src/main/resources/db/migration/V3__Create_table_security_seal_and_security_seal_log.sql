CREATE TABLE IF NOT EXISTS PUBLIC.SECURITY_SEAL
(
	ID SERIAL NOT NULL,
	SECURITY_SEAL_CODE VARCHAR(20) NOT NULL UNIQUE,
    ENTRY_ORDER_ID BIGINT NOT NULL,
    USER_CREATED_ID VARCHAR(100),
    USER_CREATED_EMAIL VARCHAR(200),
    DATE_CREATED_AT TIMESTAMP,
    USER_UPDATED_ID VARCHAR(100),
    USER_UPDATED_EMAIL VARCHAR(200),
    DATE_UPDATED_AT TIMESTAMP,
	CONSTRAINT SECURITY_SEAL_PKEY PRIMARY KEY (ID),
	CONSTRAINT FK_SECURITY_SEAL_ENTRY FOREIGN KEY (ENTRY_ORDER_ID) REFERENCES ENTRY_ORDER(ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.SECURITY_SEAL_LOG
(
	ID SERIAL NOT NULL,
	SECURITY_SEAL_ID BIGINT NOT NULL,
	ENTRY_ORDER_ID BIGINT NOT NULL,
    SECURITY_SEAL_CODE VARCHAR(20) NOT NULL,
	LAST_USER_ID VARCHAR(100),
    LAST_USER_UPDATED_EMAIL VARCHAR(200),
    LAST_DATE_UPDATED_AT TIMESTAMP,
	CONSTRAINT SECURITY_SEAL_LOG_PKEY PRIMARY KEY (ID),
	CONSTRAINT FK_SECURITY_SEAL_LOG_SEAL FOREIGN KEY (SECURITY_SEAL_ID) REFERENCES SECURITY_SEAL(ID),
	CONSTRAINT FK_SECURITY_SEAL_LOG_ENTRY FOREIGN KEY (ENTRY_ORDER_ID) REFERENCES ENTRY_ORDER(ID)
);
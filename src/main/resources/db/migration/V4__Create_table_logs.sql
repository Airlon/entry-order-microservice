CREATE TABLE IF NOT EXISTS PUBLIC.ENTRY_ORDER_LOG
(
	ID SERIAL NOT NULL,
	ENTRY_ORDER_CODE VARCHAR(20) NOT NULL,
    STATUS VARCHAR(100),
    USER_STATUS_UPDATE_ID VARCHAR(100),
    USER_STATUS_UPDATE_EMAIL VARCHAR(200),
    DATE_STATUS_UPDATE TIMESTAMP,
	CONSTRAINT ENTRY_ORDER_LOG_PKEY PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.ENTRY_ORDER_PRODUCT_LOG
(
	ID SERIAL NOT NULL,
    ENTRY_ORDER_CODE VARCHAR(20) NOT NULL,
    PRODUCT_ID BIGINT NOT NULL,
	USER_STATUS_UPDATE_ID VARCHAR(100),
    USER_STATUS_UPDATE_EMAIL VARCHAR(200),
    ENTRY_ORDER_PRODUCT_STATUS VARCHAR(50),
    DATE_STATUS_UPDATE TIMESTAMP,
	CONSTRAINT ENTRY_ORDER_PRODUCT_LOG_PKEY PRIMARY KEY (ID)
);
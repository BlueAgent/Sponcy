CREATE TABLE IF NOT EXISTS 'ShopOwner' (
	`id`	INTEGER NOT NULL,
	`uuid`	TEXT NOT NULL UNIQUE,
	`name`	TEXT NOT NULL,
	`money`	INTEGER NOT NULL DEFAULT 0 CHECK(money >= 0),
	PRIMARY KEY(`id`)
);
CREATE TABLE IF NOT EXISTS 'ShopItem' (
	`id`	INTEGER NOT NULL,
	`owner`	INTEGER NOT NULL,
	`item_id`	TEXT NOT NULL,
	`meta`	INTEGER NOT NULL DEFAULT 0,
	`nbt`	BLOB,
	`amount`	INTEGER NOT NULL DEFAULT 0 CHECK(amount >= 0),
	PRIMARY KEY(`id`),
	FOREIGN KEY(`owner`) REFERENCES `ShopOwner.id`
);
CREATE TABLE IF NOT EXISTS 'ShopShop' (
	`id`	INTEGER NOT NULL,
	`owner`	INTEGER NOT NULL,
	`money`	INTEGER NOT NULL DEFAULT 0 CHECK(money >= 0),
	`name`	TEXT NOT NULL DEFAULT '',
	`description`	INTEGER NOT NULL DEFAULT '',
	PRIMARY KEY(`id`)
    FOREIGN KEY(`owner`) REFERENCES `ShopOwner.id`
);
CREATE TABLE IF NOT EXISTS 'ShopOffer' (
	`id`	INTEGER NOT NULL,
	`shop`	INTEGER NOT NULL,
	`money`	INTEGER NOT NULL DEFAULT 0,
	`description`	TEXT NOT NULL DEFAULT '',
	PRIMARY KEY(`id`)
    FOREIGN KEY(`shop`) REFERENCES `ShopShop.id`
);
CREATE TABLE IF NOT EXISTS 'ShopOfferItems' (
	`id`	INTEGER NOT NULL,
	`offer`	INTEGER NOT NULL,
	`item`	INTEGER NOT NULL,
	`amount`	INTEGER NOT NULL DEFAULT 0 CHECK(amount != 0),
	PRIMARY KEY(`id`)
    FOREIGN KEY(`offer`) REFERENCES `ShopOffer.id`
    FOREIGN KEY(`item`) REFERENCES `ShopItem.id`
);

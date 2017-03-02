
create table sizes(
    size    varchar primary key
);
insert into sizes values('S');
insert into sizes values('L');
insert into sizes values('XL');
insert into sizes values('XXL');

create table goods_items (
    id          serial primary key,
    name        varchar not null,
    price       decimal(10,2) not null default 0,
    qnt         int not null default 0,
    display     int not null default 0,
    images      varchar array,
    description varchar
--    CONSTRAINT unique_name UNIQUE (name)
);
CREATE UNIQUE INDEX unique_goods_items_name ON goods_items (name);

insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (black)', 10.0, 1000, 1, '{"/img/cat2.jpg"}',    'Описание товара. Довольно длинное долно быть. И интересное.');
insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (green)', 10.0, 1000, 1, '{"/img/cat2.jpg"}',    'Описание товара. Довольно длинное долно быть. И интересное.');
insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (red)',   10.0, 1000, 1, '{"/img/thold2.jpg"}',  'Описание товара. Довольно длинное долно быть. И интересное.');
insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (blue)',  10.0, 1000, 1, '{"/img/thold1.jpg"}',  'Описание товара. Довольно длинное долно быть. И интересное.');
insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (yellow)',10.0, 1000, 1, '{"/img/trunks1.jpg"}', 'Описание товара. Довольно длинное долно быть. И интересное.');
insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (cyan)',  10.0, 1000, 1, '{"/img/trunks2.jpg"}', 'Описание товара. Довольно длинное долно быть. И интересное.');
insert into goods_items values(nextval('goods_items_id_seq'), 't-holder (funky)', 10.0, 1000, 1, '{"/img/thold2.jpg"}',  'Описание товара. Довольно длинное долно быть. И интересное.');

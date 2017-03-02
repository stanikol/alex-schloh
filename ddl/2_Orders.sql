create table status (
    status varchar primary key
);

insert into status values ('open');
insert into status values ('ordered');
insert into status values ('shipped');
insert into status values ('delivered');
insert into status values ('canceled');

create table orders (
    id         serial  primary key,
    order_date timestamp WITH TIME ZONE default(now() at time zone 'utc'),
    address    varchar not null,
    total      decimal(10,2) not null default 0,
    status     varchar references status(status),
    comments   varchar,
    email      varchar not null
);

insert into orders values (nextval('orders_id_seq'), '2017-01-01 01:01'::timestamp, 'ship to this address',
        100.00, 'ordered', 'coment from client',  'a@a.a');
insert into orders values (nextval('orders_id_seq'), '2017-02-02 02:02'::timestamp, 'Another order ship to another address',
        200.00, 'ordered', 'another coment from client', 'b@b.b');

create table ordered_items(
    id            serial primary key,
    order_id      int references orders(id),
    goods_item_id int references goods_items(id),
    qnt           int not null default 0,
    size          varchar references sizes(size),
    total         decimal(10,2) not null default 0
);

insert into ordered_items values (nextval('ordered_items_id_seq'), 1, 1, 11, 'XL', 110);
insert into ordered_items values (nextval('ordered_items_id_seq'), 1, 2, 2, 'XL', 20);

--create view ordered_info as
--    select gi.name, gi.price, gi.id as item_id,
--            oi.qnt, oi.size, oi.total as item_total,
--            o.email, o.status, o.order_date as order_date
--        from ordered_items oi
--            left join orders o on oi.order_id=o.id
--            left join goods_items gi on oi.goods_item_id = gi.id ;


create view ordered_full_info as
    select gi.name, gi.price, gi.id as item_id,
            oi.id as ordered_item_id, oi.qnt, oi.size, oi.total as item_total,
            o.id as order_id, o.address, o.total, o.email, o.status, o.comments, o.order_date as order_date
        from ordered_items oi left join orders o on oi.order_id=o.id left join goods_items gi on oi.goods_item_id = gi.id;
-- Datos de ejemplo para probar la API nada más arrancar

insert into warehouses (code, name, city) values
    ('ZGZ-01', 'Almacén central', 'Zaragoza'),
    ('BCN-01', 'Almacén Zona Franca', 'Barcelona'),
    ('MAD-01', 'Almacén Coslada', 'Madrid');

insert into products (sku, name, price, min_stock) values
    ('LAP-14-I5',  'Portátil 14" i5 16GB',          749.00, 5),
    ('MON-27-QHD', 'Monitor 27" QHD',               289.90, 8),
    ('KEY-MECH-ES','Teclado mecánico ES',            89.50, 10),
    ('MOU-WL',     'Ratón inalámbrico',              24.99, 20),
    ('DOCK-USBC',  'Dock USB-C 10 en 1',            119.00, 4);

insert into stock (product_id, warehouse_id, quantity)
select p.id, w.id, v.qty
from (values
    ('LAP-14-I5',   'ZGZ-01', 12), ('LAP-14-I5',   'MAD-01', 6),
    ('MON-27-QHD',  'ZGZ-01', 20), ('MON-27-QHD',  'BCN-01', 9),
    ('KEY-MECH-ES', 'BCN-01', 35),
    ('MOU-WL',      'ZGZ-01', 60), ('MOU-WL',      'MAD-01', 25),
    ('DOCK-USBC',   'MAD-01', 5)
) as v(sku, code, qty)
join products p on p.sku = v.sku
join warehouses w on w.code = v.code;

insert into customers (name, email, tax_id) values
    ('Talleres Ebro S.L.',     'compras@talleresebro.example', 'B50000001'),
    ('Oficinas Moncayo S.A.',  'it@moncayo.example',           'A50000002');

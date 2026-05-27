-- =============================================================================
-- Data de prueba para desarrollo local (perfil 'local').
--
-- En AWS este script NO se ejecuta (application-aws.yml tiene mode: never).
-- Para resetear: DROP DATABASE gamarra360; CREATE DATABASE gamarra360; → reimportar schema_clean.sql
--
-- Cubre el catálogo del CU-08 con:
--   - 1 admin + 12 comerciantes (todos verificados/activos)
--   - 12 tiendas (todas verificadas)
--   - 13 productos del catálogo del frontend (mismos IDs aproximados)
--   - Colores, tallas, categorías de catálogo
--   - Variantes (talla × color) con stock real
--   - Imágenes principales + secundarias
--   - Especificaciones técnicas
--   - Algunas reglas de descuento por volumen
--
-- ON DUPLICATE KEY IGNORE para que el script sea idempotente.
-- =============================================================================

USE gamarra360;

-- ----------------------------------------------------------------------------
-- 1) Catálogos básicos: categorías, colores, tallas
-- ----------------------------------------------------------------------------

INSERT INTO categorias (id_categoria, nombre_categoria, descripcion) VALUES
  (1, 'HOMBRE',         'Prendas para hombres adultos'),
  (2, 'MUJER',          'Prendas para mujeres adultas'),
  (3, 'NINOS',          'Ropa infantil unisex'),
  (4, 'UNISEX_ADULTOS', 'Prendas unisex para adultos'),
  (5, 'UNISEX_NINOS',   'Prendas unisex infantiles')
ON DUPLICATE KEY UPDATE nombre_categoria = VALUES(nombre_categoria);

INSERT INTO colores (id_color, nombre, cod_hex, activo) VALUES
  (1, 'Blanco',         '#F5F5F5', 1),
  (2, 'Negro',          '#1A1A1A', 1),
  (3, 'Azul Marino',    '#1B2A4E', 1),
  (4, 'Azul Clásico',   '#2A4A7F', 1),
  (5, 'Gris Oscuro',    '#3F4145', 1),
  (6, 'Rosa Palo',      '#E78BA7', 1),
  (7, 'Camel',          '#C19A6B', 1),
  (8, 'Azul',           '#5DADE2', 1),
  (9, 'Rojo',           '#E74C3C', 1),
  (10,'Verde',          '#27AE60', 1),
  (11,'Gris Melange',   '#7F8C8D', 1),
  (12,'Celeste Claro',  '#A6C7E0', 1),
  (13,'Azul Denim',     '#4A6FA5', 1)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO tallas (id_talla, talla, activo) VALUES
  (1, 'XS', 1),
  (2, 'S',  1),
  (3, 'M',  1),
  (4, 'L',  1),
  (5, 'XL', 1),
  (6, 'XXL',1),
  (7, '4',  1),
  (8, '6',  1),
  (9, '8',  1),
  (10,'10', 1)
ON DUPLICATE KEY UPDATE talla = VALUES(talla);

-- ----------------------------------------------------------------------------
-- 2) Usuarios (1 admin + 12 comerciantes activos y verificados)
-- ----------------------------------------------------------------------------
-- Contraseña de demo (en producción: BCrypt). Para local pongo un placeholder.

INSERT INTO usuarios (usuario_id, nombres, primer_apellido, email, contrasenha, activo, proveedor_auth, rol) VALUES
  (1,  'Admin',     'Sistema',   'admin@gamarra360.com',          'demo', 1, 'LOCAL', 'ADMIN'),
  (2,  'Carlos',    'Quispe',    'carlos@modaurbana.com',         'demo', 1, 'LOCAL', 'VENDEDOR'),
  (3,  'María',     'Killa',     'maria@estilokilla.com',         'demo', 1, 'LOCAL', 'VENDEDOR'),
  (4,  'Juan',      'Denim',     'juan@denimhouse.com',           'demo', 1, 'LOCAL', 'VENDEDOR'),
  (5,  'Ana',       'Samantha',  'ana@boutiquesamantha.com',      'demo', 1, 'LOCAL', 'VENDEDOR'),
  (6,  'Luis',      'Peque',     'luis@pequemoda.com',            'demo', 1, 'LOCAL', 'VENDEDOR'),
  (7,  'Pedro',     'Elegancia', 'pedro@elegancestore.com',       'demo', 1, 'LOCAL', 'VENDEDOR'),
  (8,  'Rosa',      'Urbana',    'rosa@rosaurbana.com',           'demo', 1, 'LOCAL', 'VENDEDOR'),
  (9,  'Diego',     'Street',    'diego@streetwear.com',          'demo', 1, 'LOCAL', 'VENDEDOR'),
  (10, 'Sofía',     'Cotton',    'sofia@urbancotton.com',         'demo', 1, 'LOCAL', 'VENDEDOR'),
  (11, 'Andrés',    'Sur',       'andres@textilesdelsur.com',     'demo', 1, 'LOCAL', 'VENDEDOR'),
  (12, 'Patricia',  'Mini',      'patricia@minimoda.com',         'demo', 1, 'LOCAL', 'VENDEDOR'),
  (13, 'Roberto',   'Textiles',  'roberto@textilesnova.com',      'demo', 1, 'LOCAL', 'VENDEDOR')
ON DUPLICATE KEY UPDATE nombres = VALUES(nombres);

INSERT INTO admins (usuario_id) VALUES (1)
ON DUPLICATE KEY UPDATE usuario_id = VALUES(usuario_id);

INSERT INTO comerciantes (usuario_id, razon_social, ruc, verificado) VALUES
  (2,  'Moda Urbana SAC',      '20100000002', 1),
  (3,  'Estilo Killa EIRL',    '20100000003', 1),
  (4,  'Denim House SAC',      '20100000004', 1),
  (5,  'Boutique Samantha',    '20100000005', 1),
  (6,  'Peque Moda SAC',       '20100000006', 1),
  (7,  'Elegance Store SAC',   '20100000007', 1),
  (8,  'Rosa Urbana EIRL',     '20100000008', 1),
  (9,  'Street Wear SAC',      '20100000009', 1),
  (10, 'Urban Cotton SAC',     '20100000010', 1),
  (11, 'Textiles del Sur SAC', '20100000011', 1),
  (12, 'Mini Moda SAC',        '20100000012', 1),
  (13, 'Textiles Nova SAC',    '20100000013', 1)
ON DUPLICATE KEY UPDATE razon_social = VALUES(razon_social);

-- ----------------------------------------------------------------------------
-- 3) Tiendas (verificadas)
-- ----------------------------------------------------------------------------

INSERT INTO tiendas (id_tienda, id_comerciante, nombre_comercial, informacion, foto, verificada) VALUES
  (1,  2,  'Moda Urbana Gamarra',  'Polos y básicos urbanos.',                'https://images.unsplash.com/photo-1441986300917-64674bd600d8', 1),
  (2,  3,  'Estilo Killa',         'Moda femenina con diseños actuales.',     'https://images.unsplash.com/photo-1483985988355-763728e1935b', 1),
  (3,  4,  'Denim House Perú',     'Especialistas en jeans premium.',         'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f', 1),
  (4,  5,  'Boutique Samantha',    'Boutique femenina juvenil.',              'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5', 1),
  (5,  6,  'Peque Moda Kids',      'Ropa infantil cómoda y duradera.',        'https://images.unsplash.com/photo-1503944168849-8bf86038c91b', 1),
  (6,  7,  'Elegance Store',       'Camisas y trajes formales premium.',      'https://images.unsplash.com/photo-1490481651871-ab68de25d43d', 1),
  (7,  8,  'Rosa Urbana',          'Tendencias urbanas femeninas.',           'https://images.unsplash.com/photo-1469334031218-e382a71b716b', 1),
  (8,  9,  'Street Wear Gamarra',  'Ropa urbana oversize.',                   'https://images.unsplash.com/photo-1581655353564-df123a1eb820', 1),
  (9,  10, 'Urban Cotton Co.',     'Algodón Pima premium.',                   'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5', 1),
  (10, 11, 'Textiles del Sur',     'Confecciones premium en cuero.',          'https://images.unsplash.com/photo-1441986300917-64674bd600d8', 1),
  (11, 12, 'Mini Moda Perú',       'Estampados divertidos para niños.',       'https://images.unsplash.com/photo-1503944168849-8bf86038c91b', 1),
  (12, 13, 'Textiles Nova',        'Fabricantes de uniformes.',               'https://images.unsplash.com/photo-1556905055-8f358a7a47b2', 1)
ON DUPLICATE KEY UPDATE nombre_comercial = VALUES(nombre_comercial);

-- ----------------------------------------------------------------------------
-- 4) Productos (los 12 productos del frontend + casaca biker premium)
-- ----------------------------------------------------------------------------

INSERT INTO productos (id_producto, id_tienda, nombre, descripcion, precio_base, activo, es_personalizable) VALUES
  (1,  1,  'Polo básico de algodón hombre', 'Polo de algodón Pima peruano con corte slim fit, ideal para uso diario.', 45.00, 1, 0),
  (2,  2,  'Blusa denim manga larga mujer', 'Blusa de denim ligero con corte regular fit y detalle fruncido. Personalizable.', 48.00, 1, 1),
  (3,  3,  'Jeans slim fit hombre',         'Jean slim premium con denim peruano. Cómodo y resistente.', 89.00, 1, 0),
  (5,  4,  'Vestido casual floral',         'Vestido midi de viscosa con estampado floral exclusivo.', 75.00, 1, 0),
  (6,  5,  'Conjunto deportivo infantil',   'Conjunto deportivo en French Terry hipoalergénico para niños de 4 a 10 años.', 58.00, 1, 0),
  (7,  6,  'Camisa formal hombre',          'Camisa formal en algodón Oxford 100% con corte slim ejecutivo.', 65.00, 1, 0),
  (8,  7,  'Falda plisada mujer',           'Falda midi plisada con forro interior y cremallera lateral invisible.', 42.00, 1, 0),
  (10, 8,  'Polera oversize unisex',        'Polera oversize streetwear en French Terry 280 gsm.', 72.00, 1, 0),
  (11, 9,  'T-Shirt Oversize',              'T-Shirt oversize en 100% algodón Pima 220 gsm con costura doble.', 45.00, 1, 0),
  (12, 9,  'T-Shirt Oversize Manga Corta',  'Versión manga corta drop-shoulder. Algodón Pima 220 gsm.', 45.00, 1, 0),
  (13, 10, 'Casaca de Cuero Biker Premium','Casaca de cuero genuino con cortes biker, forro premium y herrajes YKK.', 450.00, 1, 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- N:M producto_categoria
INSERT INTO producto_categoria (id_producto, id_categoria) VALUES
  (1, 1),  -- Polo → HOMBRE
  (2, 2),  -- Blusa → MUJER
  (3, 1),  -- Jeans → HOMBRE
  (5, 2),  -- Vestido → MUJER
  (6, 3),  -- Conjunto infantil → NINOS
  (7, 1),  -- Camisa → HOMBRE
  (8, 2),  -- Falda → MUJER
  (10, 4), -- Polera oversize → UNISEX_ADULTOS
  (11, 4), -- T-Shirt → UNISEX_ADULTOS
  (12, 4), -- T-Shirt MC → UNISEX_ADULTOS
  (13, 1)  -- Casaca biker → HOMBRE
ON DUPLICATE KEY UPDATE id_producto = VALUES(id_producto);

-- ----------------------------------------------------------------------------
-- 5) Imágenes (1 principal + 2 secundarias por producto, simplificado)
-- ----------------------------------------------------------------------------

INSERT INTO imagenes_producto (id_producto, url, es_principal) VALUES
  (1, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=800&q=80', 1),
  (1, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=600&q=80', 0),
  (2, 'https://images.unsplash.com/photo-1551489186-cf8726f514f8?auto=format&fit=crop&w=800&q=80', 1),
  (2, 'https://images.unsplash.com/photo-1604159624708-c5b6614128bb?auto=format&fit=crop&w=600&q=80', 0),
  (3, 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=800&q=80', 1),
  (3, 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?auto=format&fit=crop&w=600&q=80', 0),
  (5, 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=800&q=80', 1),
  (6, 'https://images.unsplash.com/photo-1518831959646-742c3a14ebf7?auto=format&fit=crop&w=800&q=80', 1),
  (7, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=800&q=80', 1),
  (8, 'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?auto=format&fit=crop&w=800&q=80', 1),
  (10,'https://images.unsplash.com/photo-1620012253295-c15cc3e65df4?auto=format&fit=crop&w=800&q=80', 1),
  (11,'https://images.unsplash.com/photo-1503341504253-dff4815485f1?auto=format&fit=crop&w=800&q=80', 1),
  (12,'https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=800&q=80', 1),
  (13,'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80', 1),
  (13,'https://images.unsplash.com/photo-1520975954732-35dd22299614?auto=format&fit=crop&w=600&q=80', 0);

-- ----------------------------------------------------------------------------
-- 6) Variantes (subset — solo casaca biker para demo del detalle)
-- ----------------------------------------------------------------------------

INSERT INTO variantes_producto (id_variante, id_producto, id_color, id_talla, sku, stock, minimo_stock, precio_ajustado, disponible) VALUES
  (1, 13, 2, 2, 'CB-NEGRO-S',  5, 2, NULL, 1),
  (2, 13, 2, 3, 'CB-NEGRO-M',  5, 2, NULL, 1),
  (3, 13, 2, 4, 'CB-NEGRO-L',  3, 2, NULL, 1),
  (4, 13, 2, 5, 'CB-NEGRO-XL', 2, 2, NULL, 1),
  -- Polo p1: blanco/negro/azul × S/M/L/XL (12 variantes)
  (5,  1, 1, 2, 'POLO-BL-S',  12, 3, NULL, 1),
  (6,  1, 1, 3, 'POLO-BL-M',  12, 3, NULL, 1),
  (7,  1, 1, 4, 'POLO-BL-L',  12, 3, NULL, 1),
  (8,  1, 1, 5, 'POLO-BL-XL', 12, 3, NULL, 1),
  (9,  1, 2, 2, 'POLO-NE-S',  12, 3, NULL, 1),
  (10, 1, 2, 3, 'POLO-NE-M',  12, 3, NULL, 1),
  (11, 1, 2, 4, 'POLO-NE-L',  12, 3, NULL, 1),
  (12, 1, 2, 5, 'POLO-NE-XL', 12, 3, NULL, 1),
  -- Blusa denim p2
  (13, 2, 12, 2, 'BD-CC-S',   9, 2, NULL, 1),
  (14, 2, 12, 3, 'BD-CC-M',   9, 2, NULL, 1),
  (15, 2, 12, 4, 'BD-CC-L',   9, 2, NULL, 1),
  -- Jeans p3 — stock bajo (gatilla "ÚLTIMAS!")
  (16, 3, 4, 2, 'JN-AC-S',    4, 5, NULL, 1),
  (17, 3, 4, 3, 'JN-AC-M',    4, 5, NULL, 1),
  (18, 3, 4, 4, 'JN-AC-L',    4, 5, NULL, 1);

-- ----------------------------------------------------------------------------
-- 7) Especificaciones técnicas (subset para demo)
-- ----------------------------------------------------------------------------

INSERT INTO especificaciones (id_producto, nombre, descripcion) VALUES
  (1,  'MATERIAL',  '100% Algodón Pima Peruano'),
  (1,  'ESTILO',    'Slim Fit Casual'),
  (1,  'MANGAS',    'Cortas con puño elástico'),
  (1,  'CUELLO',    'Polo con tres botones'),

  (2,  'MATERIAL',  '100% Algodón (Denim Ligero)'),
  (2,  'AJUSTE',    'Regular Fit con detalle fruncido'),
  (2,  'CUIDADO',   'Lavado a máquina agua fría'),
  (2,  'ESTILO',    'Casual / Urbano'),

  (3,  'MATERIAL',  'Denim premium (98% algodón, 2% elastano)'),
  (3,  'CORTE',     'Slim Fit'),
  (3,  'CIERRE',    'Cremallera YKK + botón metálico'),
  (3,  'BOLSILLOS', '5 bolsillos clásicos'),

  (13, 'MATERIAL',  'Cuero de Grano Entero (Top Grain)'),
  (13, 'FORRO',     'Satinado Premium Antitranspirante'),
  (13, 'CIERRES',   'YKK de Acero Niquelado'),
  (13, 'BOLSILLOS', '3 Exteriores + 2 Internos de Seguridad');

-- ----------------------------------------------------------------------------
-- 8) Descuentos por volumen (algunos productos con ofertas activas)
-- ----------------------------------------------------------------------------

INSERT INTO descuentos_volumen (id_producto, cantidad_minima, cantidad_maxima, porcentaje_descuento, activo) VALUES
  (1,  1,  9,   22.22, 1),   -- Polo desde 1 → 22.22% off (queda en S/ 35)
  (3,  1,  4,   10.00, 1),   -- Jean → 10% off
  (5,  1,  10,  20.00, 1),   -- Vestido → 20% off (queda en S/ 60)
  (10, 1,  5,   43.75, 1),   -- Polera oversize → 43.75% off (queda en S/ 40.50)
  (11, 1,  10,  10.00, 1),   -- T-Shirt → 10% off
  (12, 1,  10,  10.00, 1),   -- T-Shirt MC → 10% off
  (13, 1,  4,   10.00, 1);   -- Casaca biker → 10% off (queda en S/ 405)

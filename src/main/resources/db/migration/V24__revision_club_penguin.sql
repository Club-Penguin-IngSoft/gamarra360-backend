CREATE TABLE IF NOT EXISTS parametros_sistema (
    clave VARCHAR(80) PRIMARY KEY,
    valor VARCHAR(500) NOT NULL,
    descripcion VARCHAR(500),
    tipo VARCHAR(20) NOT NULL DEFAULT 'TEXTO',
    editable BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO parametros_sistema (clave, valor, descripcion, tipo, editable) VALUES
('COMISION_PLATAFORMA', '0.10', 'Porcentaje de comisión cobrado por la plataforma', 'NUMERO', TRUE),
('PLAZO_CANCELACION_MINUTOS', '30', 'Minutos disponibles para cancelar un pedido recibido', 'ENTERO', TRUE),
('DIAS_RESPUESTA_RECLAMO', '15', 'Plazo máximo de respuesta a reclamos', 'ENTERO', TRUE),
('STOCK_BAJO_UMBRAL', '5', 'Cantidad usada para alertas de inventario bajo', 'ENTERO', TRUE)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion), tipo = VALUES(tipo);

ALTER TABLE variantes_producto ADD COLUMN material VARCHAR(100) NULL;
ALTER TABLE variantes_producto ADD COLUMN calidad VARCHAR(100) NULL;
ALTER TABLE ofertas ADD COLUMN cantidad_minima INT NOT NULL DEFAULT 1;

CREATE TABLE IF NOT EXISTS mensajes_personalizacion (
    id_mensaje BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_personalizacion BIGINT NOT NULL,
    id_remitente INT NOT NULL,
    mensaje VARCHAR(1500) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensaje_personalizacion FOREIGN KEY (id_personalizacion)
        REFERENCES personalizaciones (id_personalizacion),
    CONSTRAINT fk_mensaje_remitente FOREIGN KEY (id_remitente)
        REFERENCES usuarios (usuario_id),
    INDEX idx_mensaje_personalizacion_fecha (id_personalizacion, fecha)
);

CREATE TABLE IF NOT EXISTS reclamos (
    id_reclamo BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_vendedor INT NULL,
    id_pedido BIGINT NULL,
    tipo VARCHAR(30) NOT NULL,
    asunto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(3000) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    respuesta VARCHAR(3000) NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta DATETIME NULL,
    CONSTRAINT fk_reclamo_cliente FOREIGN KEY (id_cliente) REFERENCES usuarios (usuario_id),
    CONSTRAINT fk_reclamo_vendedor FOREIGN KEY (id_vendedor) REFERENCES usuarios (usuario_id),
    CONSTRAINT fk_reclamo_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id),
    INDEX idx_reclamo_cliente_fecha (id_cliente, fecha_creacion),
    INDEX idx_reclamo_vendedor_fecha (id_vendedor, fecha_creacion),
    INDEX idx_reclamo_tipo_estado (tipo, estado)
);

CREATE TABLE IF NOT EXISTS tarifas_envio_tienda (
    id_tarifa BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_tienda INT NOT NULL,
    id_distrito INT NOT NULL,
    costo_envio DECIMAL(10,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_tarifa_tienda_distrito UNIQUE (id_tienda, id_distrito),
    CONSTRAINT fk_tarifa_tienda FOREIGN KEY (id_tienda) REFERENCES tiendas (id_tienda),
    CONSTRAINT fk_tarifa_distrito FOREIGN KEY (id_distrito) REFERENCES distritos_envio (id_distrito)
);

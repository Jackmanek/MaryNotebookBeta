CREATE TABLE recuerdo_etiqueta (
                                   recuerdo_id NUMBER NOT NULL,
                                   etiqueta_id NUMBER NOT NULL,
                                   CONSTRAINT pk_recuerdo_etiqueta PRIMARY KEY (recuerdo_id, etiqueta_id),
                                   CONSTRAINT fk_re_recuerdo FOREIGN KEY (recuerdo_id) REFERENCES recuerdos(id),
                                   CONSTRAINT fk_re_etiqueta FOREIGN KEY (etiqueta_id) REFERENCES etiqueta(id)
);
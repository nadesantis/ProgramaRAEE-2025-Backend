
package com.products.security.domain;

public enum Role {
	  ADMIN,                 // admin general del sistema
	  ADMIN_VENTAS,          //aprueba órdenes de venta
	  OPERADOR_VENTAS,       // crea órdenes de venta y ABM de clientes/productos
	  OPERADOR_LOGISTICO,    //gestiona órdenes de recolección (asignar/finalizar)
	  TECNICO,               //ve y opera solo sus órdenes de recolección
	  CLIENTE,               // registra/paga (luego lo atás a endpoints públicos)
	  USER                   // rol básico, si querés mantenerlo
	}

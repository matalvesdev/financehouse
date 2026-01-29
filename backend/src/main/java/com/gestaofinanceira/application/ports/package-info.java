/**
 * Ports (interfaces) para comunicação com a camada de infraestrutura.
 * 
 * Este pacote contém as interfaces que definem contratos entre a camada
 * de aplicação e a camada de infraestrutura, seguindo o padrão de
 * arquitetura hexagonal (Ports & Adapters).
 * 
 * Tipos de ports:
 * - Repository ports: Para persistência de dados
 * - Service ports: Para serviços externos (IA, APIs, etc.)
 * - Notification ports: Para envio de notificações
 * - File processing ports: Para processamento de arquivos
 */
package com.gestaofinanceira.application.ports;
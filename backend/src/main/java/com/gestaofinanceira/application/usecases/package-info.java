/**
 * Pacote contendo os casos de uso da aplicação.
 * 
 * Os casos de uso representam as operações de negócio da aplicação,
 * orquestrando as interações entre entidades de domínio e serviços
 * de infraestrutura através dos ports.
 * 
 * Cada caso de uso é responsável por:
 * - Validar dados de entrada
 * - Orquestrar operações de domínio
 * - Coordenar chamadas para ports de infraestrutura
 * - Retornar resultados apropriados
 * 
 * Os casos de uso seguem o padrão Command/Query Responsibility Segregation (CQRS)
 * e são organizados por domínio funcional.
 */
package com.gestaofinanceira.application.usecases;
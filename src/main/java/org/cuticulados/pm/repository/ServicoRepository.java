package org.cuticulados.pm.repository;

import java.util.List;
import java.util.Optional;

import org.cuticulados.pm.config.JpaUtil;
import org.cuticulados.pm.entity.Servico;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * Repositório responsável pelo acesso e manipulação dos dados de {@link Servico}.
 *
 * <p>Encapsula as operações de banco relacionadas aos serviços oferecidos
 * pelo salão, incluindo busca por descrição e listagem com produtos associados.</p>
 */
public class ServicoRepository {

    /**
     * Salva ou atualiza um serviço no banco de dados.
     *
     * @param servico objeto a ser persistido
     */
    public void salvar(Servico servico) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            if (servico.getId() == null) {
                em.persist(servico);
            } else {
                em.merge(servico);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao salvar servico: " + e.getMessage());
        }
    }

    /**
     * Busca um serviço pelo ID.
     *
     * @param id identificador do serviço
     * @return {@code Optional} com o serviço encontrado, ou vazio se não existir
     */
    public Optional<Servico> buscarPorId(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Servico s = em.find(Servico.class, id);
            return Optional.ofNullable(s);
        } catch (Exception e) {
            System.err.println("Erro ao buscar servico: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista todos os serviços cadastrados.
     *
     * @return lista de todos os serviços
     */
    public List<Servico> listarTodos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery("FROM Servico", Servico.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar servicos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca serviços que contenham o termo informado na descrição.
     *
     * <p>A busca é case-insensitive (não diferencia maiúsculas de minúsculas).</p>
     *
     * @param termo texto a ser pesquisado na descrição
     * @return lista de serviços que contêm o termo
     */
    public List<Servico> buscarPorDescricao(String termo) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM Servico s " +
                                    "WHERE LOWER(s.descricao) LIKE LOWER(:termo)",
                            Servico.class)
                    .setParameter("termo", "%" + termo + "%")
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar servico por descricao: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lista todos os serviços com seus produtos associados carregados via LEFT JOIN FETCH.
     *
     * <p>O {@code LEFT JOIN FETCH} garante que serviços sem produtos também
     * sejam incluídos no resultado. O {@code DISTINCT} evita duplicatas.</p>
     *
     * @return lista de serviços com seus produtos carregados
     */
    public List<Servico> listarComProdutos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT s FROM Servico s " +
                                    "LEFT JOIN FETCH s.produtosUtilizados",
                            Servico.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar servicos com produtos: " + e.getMessage());
            return List.of();
        }
    }

//=====
    /**
     * Lista todas as combinações possíveis entre serviços e produtos (CROSS JOIN).
     *
     * <p>Em JPQL, o CROSS JOIN é feito listando duas entidades no FROM sem
     * relacionamento entre elas, separadas por vírgula. Isso gera o produto
     * cartesiano: cada serviço é combinado com cada produto.</p>
     *
     * <p>Útil para visualizar quais produtos poderiam ser associados a cada serviço.</p>
     *
     * @return lista de arrays onde cada posição 0 é um Servico e posição 1 é um Produto
     */
    public List<Object[]> listarCombinacoesProdutos() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT s, p FROM Servico s, " +
                                    "org.cuticulados.pm.entity.Produto p",
                            Object[].class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao listar combinacoes: " + e.getMessage());
            return List.of();
        }
    }
//=====

    /**
     * Remove um serviço do banco de dados pelo ID.
     *
     * @param id identificador do serviço a ser removido
     */
    public void deletar(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            em.getTransaction().begin();
            Servico s = em.find(Servico.class, id);
            if (s != null) {
                em.remove(s);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            System.err.println("Erro ao deletar servico: " + e.getMessage());
        }
    }
}
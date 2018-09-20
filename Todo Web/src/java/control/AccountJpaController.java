/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import control.exceptions.IllegalOrphanException;
import control.exceptions.NonexistentEntityException;
import control.exceptions.PreexistingEntityException;
import control.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.Task;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import model.Account;

/**
 *
 * @author pakbu
 */
public class AccountJpaController implements Serializable {

    public AccountJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Account account) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (account.getTaskCollection() == null) {
            account.setTaskCollection(new ArrayList<Task>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Task> attachedTaskCollection = new ArrayList<Task>();
            for (Task taskCollectionTaskToAttach : account.getTaskCollection()) {
                taskCollectionTaskToAttach = em.getReference(taskCollectionTaskToAttach.getClass(), taskCollectionTaskToAttach.getTaskid());
                attachedTaskCollection.add(taskCollectionTaskToAttach);
            }
            account.setTaskCollection(attachedTaskCollection);
            em.persist(account);
            for (Task taskCollectionTask : account.getTaskCollection()) {
                Account oldUsernameOfTaskCollectionTask = taskCollectionTask.getUsername();
                taskCollectionTask.setUsername(account);
                taskCollectionTask = em.merge(taskCollectionTask);
                if (oldUsernameOfTaskCollectionTask != null) {
                    oldUsernameOfTaskCollectionTask.getTaskCollection().remove(taskCollectionTask);
                    oldUsernameOfTaskCollectionTask = em.merge(oldUsernameOfTaskCollectionTask);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findAccount(account.getUsername()) != null) {
                throw new PreexistingEntityException("Account " + account + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Account account) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account persistentAccount = em.find(Account.class, account.getUsername());
            Collection<Task> taskCollectionOld = persistentAccount.getTaskCollection();
            Collection<Task> taskCollectionNew = account.getTaskCollection();
            List<String> illegalOrphanMessages = null;
            for (Task taskCollectionOldTask : taskCollectionOld) {
                if (!taskCollectionNew.contains(taskCollectionOldTask)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Task " + taskCollectionOldTask + " since its username field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Task> attachedTaskCollectionNew = new ArrayList<Task>();
            for (Task taskCollectionNewTaskToAttach : taskCollectionNew) {
                taskCollectionNewTaskToAttach = em.getReference(taskCollectionNewTaskToAttach.getClass(), taskCollectionNewTaskToAttach.getTaskid());
                attachedTaskCollectionNew.add(taskCollectionNewTaskToAttach);
            }
            taskCollectionNew = attachedTaskCollectionNew;
            account.setTaskCollection(taskCollectionNew);
            account = em.merge(account);
            for (Task taskCollectionNewTask : taskCollectionNew) {
                if (!taskCollectionOld.contains(taskCollectionNewTask)) {
                    Account oldUsernameOfTaskCollectionNewTask = taskCollectionNewTask.getUsername();
                    taskCollectionNewTask.setUsername(account);
                    taskCollectionNewTask = em.merge(taskCollectionNewTask);
                    if (oldUsernameOfTaskCollectionNewTask != null && !oldUsernameOfTaskCollectionNewTask.equals(account)) {
                        oldUsernameOfTaskCollectionNewTask.getTaskCollection().remove(taskCollectionNewTask);
                        oldUsernameOfTaskCollectionNewTask = em.merge(oldUsernameOfTaskCollectionNewTask);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = account.getUsername();
                if (findAccount(id) == null) {
                    throw new NonexistentEntityException("The account with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account account;
            try {
                account = em.getReference(Account.class, id);
                account.getUsername();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The account with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Task> taskCollectionOrphanCheck = account.getTaskCollection();
            for (Task taskCollectionOrphanCheckTask : taskCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Account (" + account + ") cannot be destroyed since the Task " + taskCollectionOrphanCheckTask + " in its taskCollection field has a non-nullable username field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(account);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Account> findAccountEntities() {
        return findAccountEntities(true, -1, -1);
    }

    public List<Account> findAccountEntities(int maxResults, int firstResult) {
        return findAccountEntities(false, maxResults, firstResult);
    }

    private List<Account> findAccountEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Account.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Account findAccount(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public int getAccountCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Account> rt = cq.from(Account.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}

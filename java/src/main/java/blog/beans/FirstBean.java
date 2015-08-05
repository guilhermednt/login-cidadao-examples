package blog.beans;

import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.primefaces.component.calendar.Calendar;

import blog.modelo.BlogAuthor;
import blog.modelo.BlogPost;

@ManagedBean
@RequestScoped
public class FirstBean {

	private List<BlogPost> negociacoes;

	public FirstBean() {
		EntityManager em = getManager();
		BlogAuthor autor = null;
		Query query = null;
		query = em.createQuery("select t from BlogAuthor as t where displayName = :nome");
		query.setParameter("nome", "freeforall");
		try {			
			try {
				autor = (BlogAuthor) query.getSingleResult();
			} catch (NoResultException e) {
				em.getTransaction().begin();
				autor = new BlogAuthor();
				autor.setDisplayName("freeforall");
				autor.setFirstName("Anon");
				autor.setLastName("imo");
				em.persist(autor);
				BlogPost firstPost = new BlogPost();
				firstPost.setBlogAuthor(autor);
				firstPost.setArticle("muito longo");
				firstPost.setTitle("first blood");
				firstPost.setDatePublished(new Date());
				em.persist(firstPost);
				em.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			query = em.createQuery("select t from BlogPost as t where blogAuthor = :autor");
			query.setParameter("autor", autor);
			negociacoes = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private EntityManager getManager() {
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("tarefas");
		EntityManager manager = factory.createEntityManager();
		return manager;
	}

	public List<BlogPost> getNegociacoes() {
		System.out.println("aiai");
		return negociacoes;
	}
}
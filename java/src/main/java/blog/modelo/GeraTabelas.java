package blog.modelo;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class GeraTabelas {

	public static void main(String[] args) {
		/* baseado em http://www.jarrodoberto.com/articles/2011/11/design-pattern-blog-erd */
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("tarefas");

		factory.close();
	}
}
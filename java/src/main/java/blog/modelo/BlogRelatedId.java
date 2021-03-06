package blog.modelo;

// Generated Oct 24, 2014 1:54:18 PM by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * BlogRelatedId generated by hbm2java
 */
@Embeddable
public class BlogRelatedId implements java.io.Serializable {

	private int blogPostId;
	private int blogRelatedPostId;

	public BlogRelatedId() {
	}

	public BlogRelatedId(int blogPostId, int blogRelatedPostId) {
		this.blogPostId = blogPostId;
		this.blogRelatedPostId = blogRelatedPostId;
	}

	@Column(name = "blog_post_id", nullable = false)
	public int getBlogPostId() {
		return this.blogPostId;
	}

	public void setBlogPostId(int blogPostId) {
		this.blogPostId = blogPostId;
	}

	@Column(name = "blog_related_post_id", nullable = false)
	public int getBlogRelatedPostId() {
		return this.blogRelatedPostId;
	}

	public void setBlogRelatedPostId(int blogRelatedPostId) {
		this.blogRelatedPostId = blogRelatedPostId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof BlogRelatedId))
			return false;
		BlogRelatedId castOther = (BlogRelatedId) other;

		return (this.getBlogPostId() == castOther.getBlogPostId())
				&& (this.getBlogRelatedPostId() == castOther
						.getBlogRelatedPostId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getBlogPostId();
		result = 37 * result + this.getBlogRelatedPostId();
		return result;
	}

}

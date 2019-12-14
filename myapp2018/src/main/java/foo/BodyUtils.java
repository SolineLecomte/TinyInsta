package foo;

import java.io.Serializable;

import com.google.appengine.api.datastore.Text;

public class BodyUtils implements Serializable {
	
	/**
	 * Added automatically
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Text image;
	private String text;
	
	public Text getImage() {
		return image;
	}

	public void setImage(Text image) {
		this.image = image;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	};
	
	

}
package infovis.paracoords;

import infovis.scatterplot.Model;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseController implements MouseListener, MouseMotionListener {
	private View view = null;
	private Model model = null;
	Shape currentShape = null;

	private boolean drawingMarker = false;
	
	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		//check bounds?
		int x = e.getX();
		int y = e.getY();
		drawingMarker = true;
		view.setMarkerRect(x,y,x,y);

	}

	public void mouseReleased(MouseEvent e) {
		//check bounds?
		drawingMarker = false;
		view.brushAndLink();
		view.setMarkerRect(0,0,0,0);

	}

	public void mouseDragged(MouseEvent e) {

		int x = e.getX();
		int y = e.getY();

		if (drawingMarker){

			view.setMarkerRect((int)(view.getMarkerRect().getX()), (int)(view.getMarkerRect().getY()),
					x,y);
		}
	}

	public void mouseMoved(MouseEvent e) {

	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}

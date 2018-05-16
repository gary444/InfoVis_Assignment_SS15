package infovis.paracoords;

import infovis.scatterplot.Data;
import infovis.scatterplot.Model;
import infovis.scatterplot.Range;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class View extends JPanel {
	private Model model = null;

	private ArrayList<Integer> axis_x_positions;
	private ArrayList<Boolean> axis_is_ascending;

	private Rectangle2D markerRect = new Rectangle2D.Double(0,0,0,0);

	private int AXIS_WIDTH;
	private int AXIS_HEIGHT;
	private final double AXIS_RANGE_OFFSET = 0.1; // at each end
	private int Y_PADDING_TOP;
	private int Y_PADDING_BOTTOM;

	@Override
	public void paint(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);


		final int X_PADDING = (int)(getWidth() * 0.1);
		Y_PADDING_TOP = (int)(getHeight() * 0.15);
		Y_PADDING_BOTTOM = (int)(getHeight() * 0.1);
		AXIS_WIDTH = (int)(getWidth() * 0.005);
		AXIS_HEIGHT = (int)(getHeight() - (Y_PADDING_TOP+Y_PADDING_BOTTOM));
		ArrayList<String> labels = model.getLabels();
		int NUM_AXES = labels.size();

		//set initial x positions if necessary
		if (axis_x_positions == null){
			axis_x_positions = new ArrayList<>();
			final int AXIS_STEP = (getWidth() - X_PADDING*2 - AXIS_WIDTH) / (NUM_AXES - 1);
			for (int i = 0; i < NUM_AXES; i++){
				axis_x_positions.add(X_PADDING+(i*AXIS_STEP));
			}
		}
		//set initial ascending variables if necessary
		if (axis_is_ascending == null){
			axis_is_ascending = new ArrayList<>();
			for (int i = 0; i < NUM_AXES; i++)
				axis_is_ascending.add(true);
		}

		//title
		Font font = new Font("Serif", Font.PLAIN, (int)(Y_PADDING_TOP/2));
		g2D.setFont(font);
		g2D.drawString("Parallel Co-ordinates", (int)(getWidth() * 0.28), (int)(getHeight()* 0.06));

		//draw axes and labels
		font = new Font("Serif", Font.PLAIN, Y_PADDING_TOP/5);
		g2D.setFont(font);
		g2D.setColor(new Color(0xff333333));
		for (int i = 0; i < NUM_AXES; i++){
			//axis
			g2D.fillRect(axis_x_positions.get(i), Y_PADDING_TOP, AXIS_WIDTH, AXIS_HEIGHT);

			//draw label
			String l = labels.get(i);
			int width = g.getFontMetrics().stringWidth(l);
			g2D.drawString(l, axis_x_positions.get(i) - (width/2), (int)(Y_PADDING_TOP * 0.8));
		}

		//plot lines for items
		ArrayList<Data> data = model.getList();
		ArrayList<Range> ranges = model.getRanges();
		//for each item...
		for (Data d : data){
			ArrayList<Integer> item_y_points = new ArrayList<>();
			double [] values = d.getValues();
			//...calculate the position at which it's line crosses each axis
			for (int i = 0; i < ranges.size(); i++){
				item_y_points.add(absPointOnRange(values[i], ranges.get(i), AXIS_HEIGHT, axis_is_ascending.get(i)));

				//then draw line
				if (i > 0){
					g2D.setColor(d.getColor());
					g2D.drawLine(axis_x_positions.get(i-1) + AXIS_WIDTH, item_y_points.get(i-1)+Y_PADDING_TOP,
							axis_x_positions.get(i), item_y_points.get(i)+Y_PADDING_TOP);
				}
			}
		}

		//draw marker rectangle
		g2D.setColor(new Color(0xff0066ff));
		g2D.draw(markerRect);

	}

	//returns an int describing how many pixels along an axis a value is
	private Integer absPointOnRange(double value, Range range, int axis_height, boolean ascending){



		if (value < range.getMin() || value > range.getMax()){
			System.err.println("absPointOnRange: Value out of range");
			return -1;
		}
		//get normalised position of value within range
		double norm_pos = (value - range.getMin()) / (range.getMax() - range.getMin());

		//scale and shift to get normalised value within offset range
		norm_pos = norm_pos * (1.0 - AXIS_RANGE_OFFSET *2);
		norm_pos += AXIS_RANGE_OFFSET;

		//convert to absolute value depending on ascending or descending
		if (ascending){
			return (int)((1.0-norm_pos) * axis_height);
		}
		else {
			return (int)(norm_pos*axis_height);
		}
	}

	public Rectangle2D getMarkerRect(){
		return markerRect;
	}
	public void setMarkerRect(int markerStartX, int markerStartY, int draggedToX, int draggedToY) {

		//if start and dragged are the same, just set w and h as 0
		if (markerStartX == draggedToX){
			markerRect = new Rectangle2D.Double(markerStartX, markerStartY,0,0);
		}
		else {
			markerRect = new Rectangle2D.Double(markerStartX,markerStartY,
					draggedToX-markerStartX, draggedToY-markerStartY);
		}
		repaint();
	}

	//highlights data items selected by current marker position
	public void brushAndLink(){

		ArrayList<Integer> arraysIntersected = new ArrayList<>();
		ArrayList<Range> permittedRanges = new ArrayList<>();

		//check which axes are intersected by marker
		for (int i = 0; i < axis_x_positions.size(); i++){

			//if intersection between marker and axis...
			Rectangle2D axis = new Rectangle2D.Double(axis_x_positions.get(i), Y_PADDING_TOP, AXIS_WIDTH, AXIS_HEIGHT);
			Rectangle2D intersct = markerRect.createIntersection(axis);
			if (intersct.getWidth() > 0){
				//add to list
				arraysIntersected.add(i);

				//create permitted range
				permittedRanges.add(getPermittedRange(i));
			}
		}

		//if no axes are marked, do nothing
		if (arraysIntersected.size() == 0)
			return;

		//for each data item, check whether it crosses relevant access within range of rectangle
		Color HILITE_COL = new Color(0xffff2222);
		ArrayList<Range> ranges = model.getRanges();
		ArrayList<Data> data = model.getList();
		for (Data d : data){
			boolean shouldHighlight = false;
			for (int i = 0; i < arraysIntersected.size(); i++){

				int axis_to_check = arraysIntersected.get(i);
				Range range_to_check_in = permittedRanges.get(i);

				if (range_to_check_in.contains(d.getValues()[axis_to_check])){

					shouldHighlight = true;
					break;
				}
			}
			if (shouldHighlight)
				d.setColor(HILITE_COL);
			else
				d.setColor(new Color(0xff000000));
		}
	}

	//calculates permitted range for data values on an axis
	// with respect to what the marker is currently selecting
	// and whether the axis is inverted
	private Range getPermittedRange (int axis_id){

		//calculate allowable range from marker position
		int axisMin = Y_PADDING_TOP;
		int axisMax = getHeight() - Y_PADDING_BOTTOM;
		int markerMin = Math.max(axisMin, (int)markerRect.getY());
		int markerMax = Math.min(axisMax, (int)(markerRect.getY() + markerRect.getHeight()));

		//get min and max on 0-1 scale
		double markerMin_d = (markerMin-axisMin) / (double)AXIS_HEIGHT;
		double markerMax_d = (markerMax-axisMin) / (double)AXIS_HEIGHT;

		//scale and shift range to allow for offsets on axes
		markerMin_d = (markerMin_d - AXIS_RANGE_OFFSET) / (1.0-2*AXIS_RANGE_OFFSET);
		markerMax_d = (markerMax_d - AXIS_RANGE_OFFSET) / (1.0-2*AXIS_RANGE_OFFSET);

		//calculate absolute values WRT data range
		Range wholeRange = model.getRanges().get(axis_id);
		Range permittedRange;
		//invert scale for ascending axes
		if (axis_is_ascending.get(axis_id)){
			double tempMin_d = markerMin_d;
			markerMin_d = 1.0-markerMax_d;
			markerMax_d = 1.0-tempMin_d;

		}
		double range = wholeRange.getMax() - wholeRange.getMin();
		permittedRange = new Range(wholeRange.getMin() + range*markerMin_d,
				wholeRange.getMin() + range*markerMax_d);

		System.out.println("range for axis: " + axis_id + ", " +
				"min = " + permittedRange.getMin() + ", max = " + permittedRange.getMax());

		return permittedRange;
	}



	@Override
	public void update(Graphics g) {
		paint(g);
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
}

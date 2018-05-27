package infovis.paracoords;

import infovis.scatterplot.Data;
import infovis.scatterplot.Model;
import infovis.scatterplot.Range;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JPanel;

public class View extends JPanel {
	private Model model = null;

	private ArrayList<Integer> axis_x_positions;
	private ArrayList<Double> axis_x_rel_pos;
	private ArrayList<Integer> axis_order;
	private ArrayList<Boolean> axis_is_ascending;

	private Rectangle2D markerRect = new Rectangle2D.Double(0,0,0,0);

	private int AXIS_WIDTH;
	private int AXIS_HEIGHT;
	private final double AXIS_RANGE_OFFSET = 0.1; // at each end
	private int Y_PADDING_TOP;
	private int Y_PADDING_BOTTOM;
	private int INVERT_BTN_TOP;
	private int INVERT_BTN_SIZE;

	@Override
	public void paint(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);


		final int X_PADDING = (int)(getWidth() * 0.1);
		Y_PADDING_TOP = (int)(getHeight() * 0.15);
		Y_PADDING_BOTTOM = (int)(getHeight() * 0.1);
		AXIS_WIDTH = (int)(getWidth() * 0.005);
		AXIS_HEIGHT = (int)(getHeight() - (Y_PADDING_TOP+Y_PADDING_BOTTOM));
		INVERT_BTN_SIZE = AXIS_WIDTH*5;
		INVERT_BTN_TOP = getHeight() - (int)(Y_PADDING_BOTTOM*0.8);
		ArrayList<String> labels = model.getLabels();
		int NUM_AXES = labels.size();

		if(axis_x_rel_pos == null){
			axis_x_rel_pos = new ArrayList<>();
			final int AXIS_STEP = (getWidth() - X_PADDING*2 - AXIS_WIDTH) / (NUM_AXES - 1);
			for (int i = 0; i < NUM_AXES; i++){
				axis_x_rel_pos.add((X_PADDING+(i*AXIS_STEP))/(double)getWidth() );
			}
		}
		//set initial ascending variables if necessary
		if (axis_is_ascending == null){
			axis_is_ascending = new ArrayList<>();
			for (int i = 0; i < NUM_AXES; i++)
				axis_is_ascending.add(true);
		}
		//update order that parallel axes should be drawn in
		axis_order = getAxisOrder(axis_x_rel_pos);

		//title
		Font title_font = new Font("Serif", Font.PLAIN, (int)(Y_PADDING_TOP/2));
		g2D.setFont(title_font);
		g2D.drawString("Parallel Co-ordinates", (int)(getWidth() * 0.28), (int)(getHeight()* 0.06));

		//draw axes and labels
		ArrayList<Range> ranges = model.getRanges();
		Font att_font = new Font("Serif", Font.PLAIN, Y_PADDING_TOP/5);
		Font label_font = new Font("Serif", Font.PLAIN, Y_PADDING_TOP/8);
		g2D.setColor(new Color(0xff333333));
		for (int i = 0; i < NUM_AXES; i++){


			int axis_x = (int)(axis_x_rel_pos.get(i) * getWidth());
			//axis
			g2D.setColor(new Color(0xff333333));
			g2D.fillRect(axis_x, Y_PADDING_TOP, AXIS_WIDTH, AXIS_HEIGHT);

			//draw axis title
			g2D.setFont(att_font);
			String l = labels.get(i);
			int width = g.getFontMetrics().stringWidth(l);
			g2D.drawString(l, axis_x - (width/2), (int)(Y_PADDING_TOP * 0.8));

			//draw axis scale labels
			g2D.setFont(label_font);
			String min_s = Double.toString(ranges.get(i).getMin());
			String max_s = Double.toString(ranges.get(i).getMax());
			int min_w = g.getFontMetrics().stringWidth(min_s);
			int max_w = g.getFontMetrics().stringWidth(max_s);
			if (axis_is_ascending.get(i)){
				//draw min at bottom
				g2D.drawString(min_s, axis_x - (min_w+AXIS_WIDTH),
						(int)(getHeight() - Y_PADDING_BOTTOM - (AXIS_RANGE_OFFSET*AXIS_HEIGHT*0.7)));
				g2D.drawString(max_s, axis_x - (max_w+AXIS_WIDTH),
						(int)(Y_PADDING_TOP + (AXIS_RANGE_OFFSET*AXIS_HEIGHT*0.9)));
			}
			else {
				g2D.drawString(max_s, axis_x - (max_w+AXIS_WIDTH),
						(int)(getHeight() - Y_PADDING_BOTTOM - (AXIS_RANGE_OFFSET*AXIS_HEIGHT*0.7)));
				g2D.drawString(min_s, axis_x - (min_w+AXIS_WIDTH),
						(int)(Y_PADDING_TOP + (AXIS_RANGE_OFFSET*AXIS_HEIGHT*0.9)));
			}

			//draw invert button
			g2D.setColor(new Color(0xff000099));
			g2D.fillRoundRect(axis_x - AXIS_WIDTH*2, INVERT_BTN_TOP, INVERT_BTN_SIZE, INVERT_BTN_SIZE,
					(int)(INVERT_BTN_SIZE*0.2), (int)(INVERT_BTN_SIZE*0.2));
			String arrow;
			if (axis_is_ascending.get(i))
				arrow = "^";
			else
				arrow = "v";
			Font arrow_font = new Font("Sans-Serif", Font.PLAIN, (int)(Y_PADDING_TOP/3));
			g2D.setFont(arrow_font);
			g2D.setColor(new Color(0xffffff));
			g2D.drawString(arrow, axis_x - (int)(AXIS_WIDTH*1.7), INVERT_BTN_TOP+INVERT_BTN_SIZE);



		}




		//plot lines for items
		ArrayList<Data> data = model.getList();
		//for each item...
		for (Data d : data){
			ArrayList<Integer> item_y_points = new ArrayList<>();
			double [] values = d.getValues();
			//...calculate the position at which it's line crosses each axis
			for (int i = 0; i < NUM_AXES; i++){

				int axis_id = axis_order.get(i);

				item_y_points.add(absPointOnRange(values[axis_id], ranges.get(axis_id), AXIS_HEIGHT, axis_is_ascending.get(axis_id)));

				//then draw line
				if (i > 0){

					int prev_axis_id = axis_order.get(i-1);
					g2D.setColor(d.getColor());
					g2D.drawLine((int)(axis_x_rel_pos.get(prev_axis_id)*getWidth()) + AXIS_WIDTH, item_y_points.get(i-1)+Y_PADDING_TOP,
							(int)(axis_x_rel_pos.get(axis_id)*getWidth()), item_y_points.get(i)+Y_PADDING_TOP);
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
		for (int i = 0; i < axis_x_rel_pos.size(); i++){

			//if intersection between marker and axis...
			Rectangle2D axis = new Rectangle2D.Double(axis_x_rel_pos.get(i)*getWidth(), Y_PADDING_TOP, AXIS_WIDTH, AXIS_HEIGHT);
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

		return permittedRange;
	}

	//returns number of axis that point is contained by
	// if none, return -1
	public int pointSelectsAxis(int x, int y){

		//check y pos
		if (y < Y_PADDING_TOP || y > (getHeight() - Y_PADDING_BOTTOM)){
			//y coord out of range of axes
			return -1;
		}
		//check x pos
		for (int i = 0; i < axis_x_rel_pos.size(); i++){
			if (x >= axis_x_rel_pos.get(i)*getWidth()
					&& x <= (axis_x_rel_pos.get(i)*getWidth() + AXIS_WIDTH)){
				return i;
			}
		}
		return -1;
	}

	public boolean pointInvertsAxis(int x,int y){

		//check y pos
		if (y < INVERT_BTN_TOP || y > INVERT_BTN_TOP+INVERT_BTN_SIZE)
			return false;

		//check x pos
		for (int i = 0; i < axis_x_rel_pos.size(); i++){
			int axis_x = (int)(axis_x_rel_pos.get(i) * getWidth());

			if (x >= axis_x - AXIS_WIDTH*2
					&& x <= axis_x - AXIS_WIDTH*2 + INVERT_BTN_SIZE){
				axis_is_ascending.set(i, !axis_is_ascending.get(i));
				repaint();
				return true;
			}
		}
		return false;
	}

	//updates x position of given axis
	public void moveAxis(int axis_id, int by_x){
		double rel_change = by_x / (double)getWidth();
		axis_x_rel_pos.set(axis_id, axis_x_rel_pos.get(axis_id) + rel_change);
		repaint();
	}

	//returns a list of axis ids in order of x location (left to right)
	private ArrayList<Integer> getAxisOrder(ArrayList<Double> x_positions){
		ArrayList<Double> axes_pos = new ArrayList<>(x_positions);
		ArrayList<Integer> axis_order = new ArrayList<>();

		for (int i = 0; i < x_positions.size(); i++){

			double min_val = Collections.min(axes_pos);
			int axis = x_positions.indexOf(min_val);
			axis_order.add(axis);
			axes_pos.set(axis, Double.MAX_VALUE);
		}
		return axis_order;
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

// Swing components (JFrame, JPanel, JButton, JSlider, etc.)
 import javax.swing.*;
// Graphics, Color, Layout managers
import java.awt.*;
// For button click handling
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// List interface and ArrayList implementation
import java.util.List;
import java.util.ArrayList;
// Mouse event handling
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;   
// Slider change handling
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * bb is a custom JPanel that allows the user to:
 *  Click to place balls,Choose ball color,Choose ball size
 */
public class bb extends JPanel {

    // set a starting color and size of the ball
    Color currentColor = Color.CYAN;
    double currentSize = 50.0;
   // List storing all balls that have been created
    List<Ball> balls = new ArrayList<>();

    /*
      Inner class representing a single ball.Stores only data (position, size, color).
    */
    class Ball { 
        // coordinates,diameter,color of ball
        double x, y;      
        double size;      
        Color color;      
        // Constructor to initialize a ball
        Ball(double x, double y, double size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }

    /**
     * Constructor for bb panel.
     * Sets up mouse handling to create balls on click.
     */
    bb() {
        // Add a mouse listener using MouseAdapter
        this.addMouseListener(new MouseAdapter() {
            // Called whenever a mouse button is pressed
            public void mousePressed(MouseEvent e) {
                // Create a new ball at mouse position
                Ball newBall = new Ball(
                        e.getX(),        // X coordinate of click
                        e.getY(),        // Y coordinate of click
                        currentSize,     // Current slider size
                        currentColor     // Current selected color
                );
                // Add the new ball to the list
                balls.add(newBall);
                // Request repaint so the new ball appears immediately
                repaint();
            }
        });
    }

    /**
     * Custom painting method.
     * Responsible for drawing all balls.
     */
    @Override
    public void paintComponent(Graphics g) {
        // Clears the panel before repainting
        super.paintComponent(g);
        // Set background color to black
        this.setBackground(new Color(0, 0, 0));
        // Draw each ball in the list
        for (Ball b : balls) {
            // Sets the new color of the ball
            g.setColor(b.color);
            // Convert size (diameter) to radius
            int radius = (int) (b.size / 2);
            /*Draw filled circle centered at (x, y) the b.x-readius and b.y-radius is used 
              to make the center of the ball at the position of the cursor 
            */ 
            g.fillOval((int) (b.x - radius),(int) (b.y - radius),(int) b.size,(int) b.size);
        }
    }

    /**
     * Entry point of the program.
     * Creates the frame, panel, and UI controls.
     */
    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("bouncyball sim");
        // Set window size
        frame.setSize(2000, 2000);
        // Exit program when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create the drawing panel
        bb panel = new bb();
        // Create a control panel for UI elements
        JPanel controlpanel = new JPanel();
        controlpanel.setBackground(Color.GRAY);
        // Button to open color chooser
        JButton cbtn = new JButton("pick a color");
        // Action listener for color picker button
        cbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open color chooser dialog
                Color newCol = JColorChooser.showDialog(
                        frame,
                        "choose a color",
                        panel.currentColor
                );
                if (newCol != null) {
                    panel.currentColor = newCol;   // Update current color
                    cbtn.setBackground(newCol);    // Update button color
                }
            }
        });

        // Slider to control ball size (min=10, max=150, initial=50)
        JSlider sizeS = new JSlider(10, 150, 50);
        // Listen for slider value changes
        sizeS.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                panel.currentSize = sizeS.getValue();
            }
        });
        // Add components to control panel
        controlpanel.add(cbtn);
        controlpanel.add(new JLabel("Size"));
        controlpanel.add(sizeS);
        // Add panels to the frame
        frame.add(controlpanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        // Make the window visible
        frame.setVisible(true);
    }
}

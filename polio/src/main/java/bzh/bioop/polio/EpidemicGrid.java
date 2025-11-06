package bzh.bioop.polio;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * EpidemicGrid.java
 */
public class EpidemicGrid {

    // --- Possible states ---
    public static final int HEALTHY = 0;
    public static final int SICK = 1;
    public static final int DEAD = 2;
    public static final int VAX = 3;   // vaccinated (healthy carrier)
    public static final int CURED = 4; // recovered

    public static class EpidemicPanel extends JPanel {
        private int[][] grid;            // city matrix
        private int cellSize = 48;       // size of each cell in pixels
        private int padding = 8;         // padding around the grid
        private Set<Point> patientZero = new HashSet<>(); // positions of patient zero

        public EpidemicPanel(int rows, int cols) {
            this.grid = new int[rows][cols];
            setPreferredSize(new Dimension(cols * cellSize + 2 * padding, rows * cellSize + 2 * padding));
            setBackground(Color.WHITE);
        }

        /**
         * Update the grid and repaint
         */
        public void setGrid(int[][] newGrid) {
            if (newGrid == null) return;
            this.grid = newGrid;
            revalidate();
            repaint();
        }

        /**
         * Set patient zero position
         */
        public void setPatientZero(int i, int j) {
            patientZero.add(new Point(j, i));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int rows = grid.length;
            int cols = grid[0].length;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = padding + c * cellSize;
                    int y = padding + r * cellSize;

                    // Background
                    g2.setColor(new Color(245, 245, 245));
                    g2.fillRect(x, y, cellSize, cellSize);

                    // Border
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(x, y, cellSize, cellSize);

                    // Draw state
                    int state = grid[r][c];
                    switch (state) {
                        case HEALTHY -> drawStickman(g2, x, y, cellSize, new Color(34, 139, 34), false, false);
                        case SICK -> drawStickman(g2, x, y, cellSize, new Color(178, 34, 34), true, false);
                        case VAX -> drawStickman(g2, x, y, cellSize, new Color(218, 165, 32), false, true);
                        case CURED -> drawStickman(g2, x, y, cellSize, new Color(218, 165, 32), false, false);
                        case DEAD -> drawGravestone(g2, x, y, cellSize);
                    }

                    // Patient zero badge: small yellow star
                    if (patientZero.contains(new Point(c, r))) {
                        drawPatientZeroStar(g2, x, y, cellSize);
                    }
                }
            }

            g2.dispose();
        }

        /**
         * Draw a stickman.
         * @param leaning whether is sick
         * @param vaxBadge whether is vaccinated (blue V badge)
         */
        private void drawStickman(Graphics2D g2, int x, int y, int size, Color color, boolean leaning, boolean vaxBadge) {
            int cx = x + size/2;
            int cy = y + size/2;

            // Head
            int headR = size/5;
            int headX = cx - headR;
            int headY = cy - size/2;
            g2.setColor(color);
            g2.fillOval(headX, headY, headR*2, headR*2);
            g2.setColor(Color.BLACK);
            g2.drawOval(headX, headY, headR*2, headR*2);

            // Eyes
            int eyeR = Math.max(1, headR/4);
            g2.fillOval(cx - headR/2 - eyeR/2, headY + headR/2, eyeR, eyeR);
            g2.fillOval(cx + headR/2 - eyeR/2, headY + headR/2, eyeR, eyeR);

            // Body positions
            int bodyY = headY + headR*2;
            int bodyHeight = size/3;
            int leanOffset = leaning ? size/8 : 0; // leaning stickman

            // Body
            g2.drawLine(cx, bodyY, cx + leanOffset, bodyY + bodyHeight);

            // Arms
            int armLength = size/4;
            g2.drawLine(cx, bodyY + size/12, cx - armLength + leanOffset, bodyY + size/3);
            g2.drawLine(cx, bodyY + size/12, cx + armLength + leanOffset, bodyY + size/3);

            // Legs
            int legLength = size/4;
            g2.drawLine(cx + leanOffset, bodyY + bodyHeight, cx - legLength + leanOffset, bodyY + bodyHeight + legLength);
            g2.drawLine(cx + leanOffset, bodyY + bodyHeight, cx + legLength + leanOffset, bodyY + bodyHeight + legLength);

            // Vaccination badge
            if (vaxBadge) drawVaccinationBadge(g2, x, y);
        }

        /**
         * Draw a small "V" badge for vaccinated people
         */
        private void drawVaccinationBadge(Graphics2D g2, int x, int y) {
            int badgeSize = cellSize/5;
            g2.setColor(Color.BLUE.darker());
            g2.fillOval(x + 2, y + 2, badgeSize, badgeSize);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, badgeSize-2));
            g2.drawString("V", x + 4, y + badgeSize);
        }

        /**
         * Draw a gravestone with rounded top and RIP
         */
        private void drawGravestone(Graphics2D g2, int x, int y, int size) {
            int width = (int)(size*0.6);
            int height = (int)(size*0.7);
            int gx = x + (size - width)/2;
            int gy = y + size/6;

            // Main tombstone
            g2.setColor(new Color(200,200,200));
            g2.fillRoundRect(gx, gy, width, height, width/2, width/2);

            // Outline
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(gx, gy, width, height, width/2, width/2);

            // RIP text
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif", Font.BOLD, size/5));
            FontMetrics fm = g2.getFontMetrics();
            String rip = "RIP";
            int tx = gx + (width - fm.stringWidth(rip))/2;
            int ty = gy + height/2 + fm.getAscent()/2;
            g2.drawString(rip, tx, ty);
        }

        /**
         * Draw patient zero badge (small yellow star) on top-left
         */
        private void drawPatientZeroStar(Graphics2D g2, int x, int y, int size) {
            int starSize = size/2;
            // four pointed star so 9 points
            int[] xs  = {x + 2 + starSize/2, x + 2 + starSize*3/4, x + 2 + starSize, x + 2 + starSize*3/4, x + 2 + starSize/2,  x + 2 + starSize/4, x + 2 + 0, x + 2 + starSize/4};
            int[] ys  = {y + 2 + starSize, y + 2 + starSize*3/4, y + 2 + starSize/2, y + 2 + starSize/4, y + 2 + 0, y + 2 + starSize/4, y + 2 + starSize/2, y + 2 + starSize*3/4};
            g2.setColor(Color.YELLOW);
            g2.fillPolygon(xs, ys, 8);
        }
    }
}

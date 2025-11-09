package bzh.bioop.polio;

import javax.swing.*;
import java.awt.*;

/**
 * EpidemicGrid.java
 */
public class EpidemicGrid {

    public static class EpidemicPanel extends JPanel {
        private Person[][] matrix;        // city matrix
        private int cellSize = 48;        // size of each cell in pixels
        private int padding = 8;          // padding around the grid

        public EpidemicPanel(int rows, int cols) {
            this.matrix = new Person[rows][cols];
            setPreferredSize(new Dimension(cols * cellSize + 2 * padding, rows * cellSize + 2 * padding));
            setBackground(Color.WHITE);
        }

        public void setMatrix(Person[][] matrix) {
            this.matrix = matrix;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (matrix == null) return;

            int rows = matrix.length;
            int cols = matrix[0].length;

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

                    Person p = matrix[r][c];
                    if (p != null) {
                        boolean sick = p.getCurrentState() == Person.State.SICK;
                        boolean vaxBadge = p.isVax();
                        Color color = switch (p.getCurrentState()) {
                            case HEALTHY -> new Color(60, 179, 113);
                            case SICK -> new Color(220, 50, 60);
                            case CURED -> new Color(255, 215, 0);
                            case DEAD -> new Color(0,0,0); // won't be drawn, gravestone used
                        };

                        if (p.getCurrentState() == Person.State.DEAD) {
                            drawGravestone(g2, x, y, cellSize);
                        } else if (p.isPatientZero()) {
                            drawPatientZero(g2, x, y, cellSize, color, sick, vaxBadge);
                        } else {
                            drawStickman(g2, x, y, cellSize, color, sick, vaxBadge);
                        }
                    }
                }
            }

            g2.dispose();
        }

        /**
         * Draw a stickman at the given coordinates
         */
        private void drawStickman(Graphics2D g2, int x, int y, int size, Color color, boolean sick, boolean vaxBadge) {
            int cx = x + size / 2;
            int cy = y + size / 2;

            // Head
            int headR = size / 5;
            int headX = cx - headR;
            int headY = cy - size / 2;
            g2.setColor(color);
            g2.fillOval(headX, headY, headR * 2, headR * 2);
            g2.setColor(Color.BLACK);
            g2.drawOval(headX, headY, headR * 2, headR * 2);

            // Eyes
            int eyeR = Math.max(1, headR / 4);
            g2.fillOval(cx - headR / 2 - eyeR / 2, headY + headR / 2, eyeR, eyeR);
            g2.fillOval(cx + headR / 2 - eyeR / 2, headY + headR / 2, eyeR, eyeR);

            // Mask if sick
            if (sick) drawMask(g2, headX, headY, headR);

            // Body
            g2.setColor(Color.BLACK);
            int bodyY = headY + headR * 2;
            int bodyHeight = size / 3;
            g2.drawLine(cx, bodyY, cx, bodyY + bodyHeight);

            // Arms
            int armLength = size / 4;
            g2.drawLine(cx, bodyY + size / 12, cx - armLength, bodyY + size / 3);
            g2.drawLine(cx, bodyY + size / 12, cx + armLength, bodyY + size / 3);

            // Legs
            int legLength = size / 4;
            g2.drawLine(cx, bodyY + bodyHeight, cx - legLength, bodyY + bodyHeight + legLength);
            g2.drawLine(cx, bodyY + bodyHeight, cx + legLength, bodyY + bodyHeight + legLength);

            // Vaccination badge
            if (vaxBadge) drawVaccinationBadge(g2, x, y);

            // Reset stroke to default
            g2.setStroke(new BasicStroke(1f));
        }

        /**
         * Draw a vaccination badge on top-left of the cell
         */
        private void drawVaccinationBadge(Graphics2D g2, int x, int y) {
            int badgeSize = cellSize / 5;
            g2.setColor(Color.BLUE.darker());
            g2.fillOval(x + 2, y + 2, badgeSize, badgeSize);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, badgeSize - 2));
            g2.drawString("V", x + 4, y + badgeSize);
        }

        /**
         * Draw a gravestone for dead person
         */
        private void drawGravestone(Graphics2D g2, int x, int y, int size) {
            int width = (int) (size * 0.5);
            int height = (int) (size * 0.7);
            int arc    = width / 4;
            int gx = x + (size - width) / 2;
            int gy = y + (size - height)/ 2;

            g2.setColor(new Color(190, 190, 190));
            g2.fillRoundRect(gx, gy, width, height, arc, arc);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(gx, gy, width, height, arc, arc);

            int baseH = height / 8;
            g2.setColor(new Color(160, 160, 160));
            g2.fillRect(gx - width/8, gy + height - baseH, width + width/4, baseH);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(gx - width/8, gy + height - baseH, width + width/4, baseH);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif", Font.BOLD, size / 6));
            FontMetrics fm = g2.getFontMetrics();
            String rip = "R.I.P";
            int tx = gx + (width - fm.stringWidth(rip)) / 2;
            int ty = gy + height / 2 + fm.getAscent() / 3;
            g2.drawString(rip, tx, ty);

            // Reset stroke
            g2.setStroke(new BasicStroke(1f));
        }

        /**
         * Draw patient zero with badge
         */
        private void drawPatientZero(Graphics2D g2, int x, int y, int size, Color color, boolean sick, boolean vaxBadge) {
            drawStickman(g2, x, y, size, color, sick, vaxBadge);

            // Badge P₀ top-left
            int badgeSize = size / 3;
            int badgeX = x + 2;
            int badgeY = y + 2;

            g2.setColor(Color.RED);
            g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(badgeX, badgeY, badgeSize, badgeSize);

            g2.setFont(new Font("SansSerif", Font.BOLD, badgeSize / 2));
            FontMetrics fm = g2.getFontMetrics();
            String text = "P₀";
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int textX = badgeX + (badgeSize - textWidth) / 2;
            int textY = badgeY + (badgeSize + textHeight / 2) / 2;
            g2.drawString(text, textX, textY);

            // Reset stroke
            g2.setStroke(new BasicStroke(1f));
        }

        /**
         * Draw a mask for sick persons
         */
        private void drawMask(Graphics2D g2, int headX, int headY, int headR) {
            g2.setColor(new Color(255, 255, 255));
            int maskWidth = (int)(headR * 1.4);
            int maskHeight = (int)(headR * 0.8);
            int maskX = headX + headR - maskWidth / 2;
            int maskY = headY + headR;
            g2.fillRoundRect(maskX, maskY, maskWidth, maskHeight, 5, 5);

            // Mask straps
            g2.setColor(new Color(220, 220, 220));
            g2.drawLine(maskX, maskY + maskHeight / 2, maskX - 3, maskY + maskHeight / 2 - 1);
            g2.drawLine(maskX + maskWidth, maskY + maskHeight / 2, maskX + maskWidth + 3, maskY + maskHeight / 2 - 1);
        }
    }
}

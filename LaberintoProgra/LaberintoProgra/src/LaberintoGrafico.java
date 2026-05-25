import javax.swing.*;
import java.awt.*;

public class LaberintoGrafico extends JPanel {

    // ── Laberinto activo ────────────────────────────────────────────────────
    private int[][] laberinto;
    private int TAM = 60; // Tamaño dinámico

    // ── Variables globales de métricas (Salida por Terminal) ────────────────
    private int  llamadas   = 0;
    private int  retrocesos = 0;
    private long inicio;
    private long fin;

    // ── RETO 4: métricas extra ──────────────────────────────────────────────
    private int profundidad = 0;
    private int nodos       = 0;

    // ── Control de Retos ────────────────────────────────────────────────────
    private int tipoRetoActivo = 0;

    // ═══════════════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {

        // ── VENTANA 1: elegir tamaño de matriz ──────────────────────────────
        String[] tamanos = {
                "1 - Matriz 5x5",
                "2 - Matriz 10x10",
                "3 - Matriz 20x20"
        };

        String tamanoElegido = (String) JOptionPane.showInputDialog(
                null,
                "Selecciona el tamaño de la matriz:",
                "Paso 1 - Tamaño del laberinto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                tamanos,
                tamanos[0]
        );

        if (tamanoElegido == null) return; 

        int indiceTamano = 0;
        for (int i = 0; i < tamanos.length; i++) {
            if (tamanos[i].equals(tamanoElegido)) { indiceTamano = i; break; }
        }

        // ── VENTANA 2: elegir tipo de reto ───────────────────────────────────
        String[] retos = {
                "RETO 1 - Medir impacto del tamaño",
                "RETO 2 - Antes (Arriba, Derecha, Abajo, Izquierda)",
                "RETO 2 - Después (Derecha primero)",
                "RETO 3 - Laberinto sin solución",
                "RETO 4 - Visualización avanzada",
                "RETO 5 - Heurística Manhattan"
        };

        String retoElegido = (String) JOptionPane.showInputDialog(
                null,
                "Selecciona el reto a aplicar:",
                "Paso 2 - Tipo de reto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                retos,
                retos[0]
        );

        if (retoElegido == null) return;

        int indiceReto = 0;
        for (int i = 0; i < retos.length; i++) {
            if (retos[i].equals(retoElegido)) { indiceReto = i; break; }
        }

        LaberintoGrafico panel = new LaberintoGrafico();
        panel.tipoRetoActivo = indiceReto; 

        JFrame ventana = new JFrame("Backtracking  |  " + tamanoElegido + "  |  " + retoElegido);
        ventana.setLayout(new BorderLayout());
        ventana.add(panel, BorderLayout.CENTER);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ── Asignar laberinto y ajustar tamaño visual dinámicamente ──────────
        if (indiceReto == 3) { // RETO 3 (Sin solución)
            if (indiceTamano == 0) {
                panel.laberinto = panel.getSinSolucion5x5();
                panel.TAM = 80;
            } else if (indiceTamano == 1) {
                panel.laberinto = panel.getSinSolucion10x10();
                panel.TAM = 50;
            } else {
                panel.laberinto = panel.getSinSolucion20x20();
                panel.TAM = 35; // Más pequeño para que quepa en pantalla
            }
        } else { // Demás Retos
            if (indiceTamano == 0) {
                panel.laberinto = panel.get5x5();
                panel.TAM = 80;
            } else if (indiceTamano == 1) {
                panel.laberinto = panel.get10x10();
                panel.TAM = 50;
            } else {
                panel.laberinto = panel.get20x20();
                panel.TAM = 35; // Más pequeño para que quepa en pantalla
            }
        }

        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        // ── Ejecutar backtracking en hilo separado ───────────────────────────
        final int retoFinal = indiceReto;
        new Thread(() -> {

            panel.llamadas    = 0;
            panel.retrocesos  = 0;
            panel.profundidad = 0;
            panel.nodos       = 0;

            panel.inicio = System.nanoTime();
            boolean solucion;

            if (retoFinal == 5) {
                solucion = panel.resolverHeuristico(0, 0);
            } else {
                solucion = panel.resolver(0, 0);
            }

            panel.fin = System.nanoTime();
            panel.repaint();

            // Resultados por terminal
            String res = (solucion ? "Solución encontrada" : "Sin solución")
                    + "\nLlamadas recursivas : " + panel.llamadas
                    + "\nRetrocesos          : " + panel.retrocesos
                    + "\nTiempo (ms)         : " + String.format("%.3f", (panel.fin - panel.inicio) / 1_000_000.0);

            if (retoFinal == 4) {
                res += "\nProfundidad máxima  : " + panel.profundidad
                        + "\nNodos explorados    : " + panel.nodos;
            }

            System.out.println("=== " + tamanoElegido + " | " + retoElegido + " ===");
            System.out.println(res);

        }).start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PINTAR EL LABERINTO
    // ═══════════════════════════════════════════════════════════════════════
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (laberinto == null) return;

        int filas = laberinto.length;
        int cols  = laberinto[0].length;

        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < cols; col++) {
                switch (laberinto[fila][col]) {
                    case 0: g.setColor(Color.WHITE);             break; 
                    case 1: g.setColor(Color.BLACK);             break; 
                    case 2: g.setColor(new Color(30, 120, 255)); break; 
                    case 9: g.setColor(new Color(50, 200, 80));  break; 
                    case 5: g.setColor(new Color(220, 60, 60));  break; 
                }
                g.fillRect(col * TAM, fila * TAM, TAM, TAM);
                g.setColor(Color.GRAY);
                g.drawRect(col * TAM, fila * TAM, TAM, TAM);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (laberinto == null) return new Dimension(400, 400);
        return new Dimension(laberinto[0].length * TAM + 2, laberinto.length * TAM);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ALGORITMO DE BACKTRACKING 
    // ═══════════════════════════════════════════════════════════════════════
    public boolean resolver(int fila, int col) {
        return resolver(fila, col, 0);
    }

    private boolean resolver(int fila, int col, int prof) {
        repaint();
        dormir();

        llamadas++;
        if (prof > profundidad) profundidad = prof;

        if (fila < 0 || col < 0 ||
                fila >= laberinto.length ||
                col >= laberinto[0].length) {
            return false;
        }

        if (laberinto[fila][col] == 1 ||
                laberinto[fila][col] == 9 ||
                laberinto[fila][col] == 5) {
            return false;
        }

        if (laberinto[fila][col] == 2) {
            return true;
        }

        laberinto[fila][col] = 9;
        nodos++;
        repaint();
        dormir();

        // ── Explorar con lógica explícita de 'if' según el Reto ──

        if (tipoRetoActivo == 2) { 
            // RETO 2 - Después: Derecha primero (Luego Abajo para evitar subir innecesariamente)
            if (resolver(fila, col + 1, prof + 1)) return true; // 1. Derecha
            if (resolver(fila + 1, col, prof + 1)) return true; // 2. Abajo
            if (resolver(fila - 1, col, prof + 1)) return true; // 3. Arriba
            if (resolver(fila, col - 1, prof + 1)) return true; // 4. Izquierda
        } 
        else { 
            // ORDEN ANTES (y Resto de Retos): Arriba, Derecha, Abajo, Izquierda
            if (resolver(fila - 1, col, prof + 1)) return true; // 1. Arriba
            if (resolver(fila, col + 1, prof + 1)) return true; // 2. Derecha
            if (resolver(fila + 1, col, prof + 1)) return true; // 3. Abajo
            if (resolver(fila, col - 1, prof + 1)) return true; // 4. Izquierda
        }

        laberinto[fila][col] = 5;
        retrocesos++;
        repaint();
        dormir();

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RETO 5 — Resolver con heurística de distancia Manhattan
    // ═══════════════════════════════════════════════════════════════════════
    public boolean resolverHeuristico(int fila, int col) {
        int filaMeta = 0, colMeta = 0;
        for (int f = 0; f < laberinto.length; f++)
            for (int c = 0; c < laberinto[0].length; c++)
                if (laberinto[f][c] == 2) { filaMeta = f; colMeta = c; }

        return resolverHeuristico(fila, col, 0, filaMeta, colMeta);
    }

    private boolean resolverHeuristico(int fila, int col, int prof, int filaMeta, int colMeta) {
        repaint();
        dormir();

        llamadas++;
        if (prof > profundidad) profundidad = prof;

        if (fila < 0 || col < 0 ||
                fila >= laberinto.length ||
                col >= laberinto[0].length) {
            return false;
        }

        if (laberinto[fila][col] == 1 ||
                laberinto[fila][col] == 9 ||
                laberinto[fila][col] == 5) {
            return false;
        }

        if (laberinto[fila][col] == 2) {
            return true;
        }

        laberinto[fila][col] = 9;
        nodos++;
        repaint();
        dormir();

        int[][] dirs = {{-1,0},{0,1},{1,0},{0,-1}};
        for (int i = 0; i < dirs.length - 1; i++) {
            for (int j = 0; j < dirs.length - 1 - i; j++) {
                int distA = Math.abs((fila + dirs[j][0])   - filaMeta)
                        + Math.abs((col  + dirs[j][1])   - colMeta);
                int distB = Math.abs((fila + dirs[j+1][0]) - filaMeta)
                        + Math.abs((col  + dirs[j+1][1]) - colMeta);
                if (distA > distB) {
                    int[] tmp  = dirs[j];
                    dirs[j]    = dirs[j+1];
                    dirs[j+1]  = tmp;
                }
            }
        }

        for (int[] dir : dirs) {
            if (resolverHeuristico(fila + dir[0], col + dir[1], prof + 1, filaMeta, colMeta))
                return true;
        }

        laberinto[fila][col] = 5;
        retrocesos++;
        repaint();
        dormir();

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITARIO
    // ═══════════════════════════════════════════════════════════════════════
    public void dormir() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RETO 1 — Laberintos con solución (3 tamaños)
    // ═══════════════════════════════════════════════════════════════════════
    public int[][] get5x5() {
        return new int[][] {
                {0, 1, 0, 0, 0},
                {0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0},
                {0, 0, 0, 0, 2}
        };
    }

    public int[][] get10x10() {
        return new int[][] {
                {0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {1, 1, 1, 0, 1, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 2}
        };
    }

    public int[][] get20x20() {
        return new int[][] {
                {0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0},
                {0,1,0,1,1,0,1,0,1,1,0,1,0,1,1,0,1,0,1,0},
                {0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0},
                {1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,1,1,0,0,1,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,0,1,0,1,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,2}
        };
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RETO 3 — Laberintos sin solución (3 tamaños)
    // ═══════════════════════════════════════════════════════════════════════
    public int[][] getSinSolucion5x5() {
        return new int[][] {
                {0, 0, 0, 0, 0},
                {0, 1, 0, 1, 0},
                {1, 1, 1, 1, 1},  // barrera completa
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 2}
        };
    }

    public int[][] getSinSolucion10x10() {
        return new int[][] {
                {0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},  // barrera completa
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 2}
        };
    }

    public int[][] getSinSolucion20x20() {
        return new int[][] {
                {0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0},
                {0,1,0,1,1,0,1,0,1,1,0,1,0,1,1,0,1,0,1,0},
                {0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0},
                {1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,1,1,0,0,1,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,0,1,0,1,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},  // barrera completa
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,2}
        };
    }
}

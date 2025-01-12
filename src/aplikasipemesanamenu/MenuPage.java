package aplikasipemesanamenu;

import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;

public class MenuPage extends javax.swing.JFrame {

    private int subtotal = 0;
    private double pajak = 0;
    int PPN = 0;
    private int total = 0;
    private int tunai = 0;
    private int kembali = 0;

    public MenuPage() {
        initComponents();
        setTime();
        setImg();
        setExtendedState(MenuPage.MAXIMIZED_BOTH);
    }

    public void setTime() {
        MethodClass.setTime(waktuLabel, tanggalLabel);
    }

    public void setImg() {
        MethodClass.setIconLabel(picMenu1, "/Image/1combomeals.jpg");
        MethodClass.setIconLabel(picMenu2, "/Image/1chickencombo.jpg");
        MethodClass.setIconLabel(picMenu3, "/Image/1Tbone.jpg");
        MethodClass.setIconLabel(picMenu4, "/Image/1ribeye.jpg");
        MethodClass.setIconLabel(picMenu5, "/Image/2Expresso.jpg");
        MethodClass.setIconLabel(picMenu6, "/Image/2Lemoncucumber.jpg");
        MethodClass.setIconLabel(picMenu7, "/Image/3applecrumble.jpg");
        MethodClass.setIconLabel(picMenu8, "/Image/3cremebrulee.jpg");
    }

    public void reset() {
        subtotal = 0;
        pajak = 0.0;
        PPN = 0;
        total = 0;
        tunai = 0;
        qtyMenu1.setValue(0);
        qtyMenu2.setValue(0);
        qtyMenu3.setValue(0);
        qtyMenu4.setValue(0);
        qtyMenu5.setValue(0);
        qtyMenu8.setValue(0);
        outSubtotal.setText("");
        outPajak.setText("");
        outTotal.setText("");
        outPesanan.setText("");
        inTunai.setText("");
        inNamaCust.setText("");
        inNoMeja.setText("");
        addMenu1.setSelected(false);
        addMenu2.setSelected(false);
        addMenu3.setSelected(false);
        addMenu4.setSelected(false);
        addMenu5.setSelected(false);
        addMenu8.setSelected(false);
        btnBayar.setEnabled(true);
        orders.clear();
    }

    public void hitung() {
        pajak = subtotal * 0.11;
        PPN = (int) Math.round(pajak);
        total = subtotal + PPN;
        outSubtotal.setText("Rp. " + String.valueOf(subtotal));
        outPajak.setText("Rp. " + String.valueOf(PPN));
        outTotal.setText("Rp. " + String.valueOf(total));
    }

    private List<Map<String, Object>> orders = new ArrayList<>();

    public void prosesOrder(JSpinner qtyMenu, JCheckBox addMenu, JLabel labelMenu, int idProduk) {
        int qty = Integer.parseInt(qtyMenu.getValue().toString()); // Ambil nilai qty dari spinner

        if (qty > 0 && addMenu.isSelected()) { // Cek apakah qty > 0 dan checkbox dicentang
            try (Connection conn = DBConnection.getConnection()) {
                // Ambil data produk dari database
                String querySelect = "SELECT stok, harga, nama FROM tb_produk WHERE id_produk = ?";
                PreparedStatement stmtSelect = conn.prepareStatement(querySelect);
                stmtSelect.setInt(1, idProduk);
                ResultSet rs = stmtSelect.executeQuery();

                if (rs.next()) { // Jika data produk ditemukan
                    int stok = rs.getInt("stok");
                    int harga = rs.getInt("harga");
                    String namaProduk = rs.getString("nama");

                    // Cek apakah stok cukup
                    if (stok >= qty) {
                        int totalHarga = qty * harga;

                        // Tambahkan ke pesanan sementara
                        Map<String, Object> order = new HashMap<>();
                        order.put("id_produk", idProduk);
                        order.put("nama_produk", namaProduk);
                        order.put("qty", qty);
                        order.put("harga_total", totalHarga);
                        order.put("stok_akhir", stok - qty);
                        orders.add(order);

                        // Perbarui subtotal
                        subtotal += totalHarga;

                        // Perbarui tampilan outPesanan
                        updateOutPesanan();

                        // Hitung subtotal, pajak, dan total
                        hitung();

                    } else { // Jika stok tidak mencukupi
                        JOptionPane.showMessageDialog(null,
                                "Stok tidak mencukupi untuk " + namaProduk,
                                "Stok Tidak Cukup",
                                JOptionPane.ERROR_MESSAGE);
                        addMenu.setSelected(false); // Batalkan checkbox
                    }
                } else { // Jika produk tidak ditemukan di database
                    JOptionPane.showMessageDialog(null,
                            "Produk tidak ditemukan di database!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    addMenu.setSelected(false); // Batalkan checkbox
                }
            } catch (SQLException e) { // Tangani error database
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Terjadi kesalahan pada database: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (qty == 0 || !addMenu.isSelected()) { // Jika qty = 0 atau checkbox tidak dicentang
            JOptionPane.showMessageDialog(null,
                    "Silakan masukkan jumlah qty yang valid dan centang checkbox!",
                    "Input Tidak Valid",
                    JOptionPane.WARNING_MESSAGE);
            addMenu.setSelected(false);
        }
    }

// Method untuk memperbarui tampilan di JTextArea
    private void updateOutPesanan() {
        // Header sesuai format
        StringBuilder pesananText = new StringBuilder("***************** Restaurant ****************\n");
        pesananText.append("Time: ").append(waktuLabel.getText()).append("\n");
        pesananText.append("Date: ").append(tanggalLabel.getText()).append("\n");
        pesananText.append("*********************************************\n");
        pesananText.append(String.format("%-3s%-15s%10s%12s\n", "", "Produk", "jumlah", "Total"));

        // Tambahkan setiap pesanan ke dalam JTextArea
        int no = 1;
        for (Map<String, Object> order : orders) {
            String namaProduk = (String) order.get("nama_produk");
            int qty = (int) order.get("qty");
            int hargaTotal = (int) order.get("harga_total");

            // Format setiap baris produk
            pesananText.append(String.format("%-3d%-15s%7d%16d\n", no++, namaProduk, qty, hargaTotal));
        }

        // Perbarui tampilan ke JTextArea
        outPesanan.setText(pesananText.toString());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new Custom.Panel();
        jLabel61 = new javax.swing.JLabel();
        waktuLabel = new javax.swing.JLabel();
        tanggalLabel = new javax.swing.JLabel();
        rightPanel = new Custom.Panel();
        jLabel49 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        outPesanan = new javax.swing.JTextArea();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        outSubtotal = new javax.swing.JLabel();
        outPajak = new javax.swing.JLabel();
        outTotal = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        btnReset = new Custom.Button();
        btnPrint = new Custom.Button();
        btnBayar = new Custom.Button();
        inNamaCust = new javax.swing.JTextField();
        inNoMeja = new javax.swing.JTextField();
        inTunai = new javax.swing.JTextField();
        panelBG = new Custom.Panel();
        panelMenu1 = new Custom.Panel();
        picMenu1 = new javax.swing.JLabel();
        labelMenu1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        addMenu1 = new javax.swing.JCheckBox();
        hargaMenu1 = new javax.swing.JLabel();
        qtyMenu1 = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        panelMenu5 = new Custom.Panel();
        addMenu5 = new javax.swing.JCheckBox();
        picMenu5 = new javax.swing.JLabel();
        hargaMenu5 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        qtyMenu5 = new javax.swing.JSpinner();
        labelMenu5 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        panelMenu6 = new Custom.Panel();
        hargaMenu6 = new javax.swing.JLabel();
        picMenu6 = new javax.swing.JLabel();
        labelMenu6 = new javax.swing.JLabel();
        qtyMenu6 = new javax.swing.JSpinner();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        addMenu6 = new javax.swing.JCheckBox();
        jLabel35 = new javax.swing.JLabel();
        panelMenu2 = new Custom.Panel();
        labelMenu2 = new javax.swing.JLabel();
        addMenu2 = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        qtyMenu2 = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        hargaMenu2 = new javax.swing.JLabel();
        picMenu2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        panelMenu7 = new Custom.Panel();
        hargaMenu7 = new javax.swing.JLabel();
        labelMenu7 = new javax.swing.JLabel();
        picMenu7 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        addMenu7 = new javax.swing.JCheckBox();
        jLabel42 = new javax.swing.JLabel();
        qtyMenu7 = new javax.swing.JSpinner();
        panelMenu3 = new Custom.Panel();
        jLabel15 = new javax.swing.JLabel();
        qtyMenu3 = new javax.swing.JSpinner();
        hargaMenu3 = new javax.swing.JLabel();
        picMenu3 = new javax.swing.JLabel();
        labelMenu3 = new javax.swing.JLabel();
        addMenu3 = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        panelMenu4 = new Custom.Panel();
        addMenu4 = new javax.swing.JCheckBox();
        labelMenu4 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        hargaMenu4 = new javax.swing.JLabel();
        qtyMenu4 = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        picMenu4 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        panelMenu8 = new Custom.Panel();
        jLabel47 = new javax.swing.JLabel();
        labelMenu8 = new javax.swing.JLabel();
        addMenu8 = new javax.swing.JCheckBox();
        jLabel46 = new javax.swing.JLabel();
        qtyMenu8 = new javax.swing.JSpinner();
        jLabel48 = new javax.swing.JLabel();
        picMenu8 = new javax.swing.JLabel();
        hargaMenu8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 242, 232));

        topPanel.setBackground(new java.awt.Color(255, 255, 255));
        topPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(159, 159, 158)));
        topPanel.setPreferredSize(new java.awt.Dimension(1540, 80));

        jLabel61.setFont(new java.awt.Font("Segoe UI Black", 0, 36)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(252, 128, 25));
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel61.setText("Menu Restoran");

        waktuLabel.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        waktuLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        waktuLabel.setPreferredSize(new java.awt.Dimension(60, 22));

        tanggalLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tanggalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tanggalLabel.setPreferredSize(new java.awt.Dimension(60, 22));

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(360, 360, 360)
                .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(waktuLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tanggalLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(topPanelLayout.createSequentialGroup()
                        .addComponent(waktuLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tanggalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        rightPanel.setBackground(new java.awt.Color(255, 255, 255));
        rightPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(159, 159, 158)));
        rightPanel.setPreferredSize(new java.awt.Dimension(390, 0));

        jLabel49.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel49.setText("Nama Pelanggan");

        jLabel51.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel51.setText("Nomor Meja");

        outPesanan.setColumns(20);
        outPesanan.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        outPesanan.setRows(5);
        jScrollPane1.setViewportView(outPesanan);

        jLabel53.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel53.setText("Subtotal");

        jLabel54.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel54.setText("Pajak 11%");

        jLabel55.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel55.setText("Total");

        outSubtotal.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        outSubtotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(159, 159, 158)));

        outPajak.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        outPajak.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(159, 159, 158)));

        outTotal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        outTotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(159, 159, 158)));

        jLabel59.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel59.setText("Tunai");

        btnReset.setForeground(new java.awt.Color(255, 255, 255));
        btnReset.setText("Reset");
        btnReset.setColor(new java.awt.Color(51, 51, 255));
        btnReset.setColorBorder(new java.awt.Color(51, 51, 255));
        btnReset.setColorClick(new java.awt.Color(0, 0, 153));
        btnReset.setColorDisabled(new java.awt.Color(204, 204, 204));
        btnReset.setColorOver(new java.awt.Color(0, 0, 153));
        btnReset.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnReset.setIconTextGap(2);
        btnReset.setRadius(10);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnPrint.setBackground(new java.awt.Color(9, 170, 41));
        btnPrint.setForeground(new java.awt.Color(255, 255, 255));
        btnPrint.setText("Print");
        btnPrint.setColor(new java.awt.Color(9, 170, 41));
        btnPrint.setColorBorder(new java.awt.Color(9, 170, 41));
        btnPrint.setColorClick(new java.awt.Color(9, 118, 51));
        btnPrint.setColorDisabled(new java.awt.Color(204, 204, 204));
        btnPrint.setColorOver(new java.awt.Color(9, 118, 51));
        btnPrint.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnPrint.setIconTextGap(2);
        btnPrint.setRadius(10);
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnBayar.setBackground(new java.awt.Color(252, 128, 25));
        btnBayar.setForeground(new java.awt.Color(255, 255, 255));
        btnBayar.setText("Bayar");
        btnBayar.setColor(new java.awt.Color(252, 128, 25));
        btnBayar.setColorBorder(new java.awt.Color(252, 128, 25));
        btnBayar.setColorClick(new java.awt.Color(252, 62, 43));
        btnBayar.setColorDisabled(new java.awt.Color(204, 204, 204));
        btnBayar.setColorOver(new java.awt.Color(252, 62, 43));
        btnBayar.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnBayar.setIconTextGap(2);
        btnBayar.setRadius(10);
        btnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarActionPerformed(evt);
            }
        });

        inTunai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inTunaiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(37, 37, 37)
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inNamaCust, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(inNoMeja, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel53, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel54, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                                .addComponent(jLabel55, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel59, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(outSubtotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                            .addComponent(outPajak, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outTotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightPanelLayout.createSequentialGroup()
                                .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(inTunai, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inNamaCust, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inNoMeja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(outSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(outPajak, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(outTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inTunai)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );

        panelBG.setBackground(new java.awt.Color(255, 242, 232));

        panelMenu1.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu1.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu1.setRoundBottomLeft(10);
        panelMenu1.setRoundBottomRight(10);
        panelMenu1.setRoundTopLeft(10);
        panelMenu1.setRoundTopRight(10);

        picMenu1.setBackground(new java.awt.Color(255, 255, 255));

        labelMenu1.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu1.setText("Combo Meals");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Harga");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Jumlah");

        addMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu1ActionPerformed(evt);
            }
        });

        hargaMenu1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu1.setText("Rp 241.000");

        qtyMenu1.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Tambahkan");

        javax.swing.GroupLayout panelMenu1Layout = new javax.swing.GroupLayout(panelMenu1);
        panelMenu1.setLayout(panelMenu1Layout);
        panelMenu1Layout.setHorizontalGroup(
            panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))
                    .addGroup(panelMenu1Layout.createSequentialGroup()
                        .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu1)
                            .addComponent(qtyMenu1)
                            .addComponent(hargaMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelMenu1Layout.setVerticalGroup(
            panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(hargaMenu1))
                .addGap(8, 8, 8)
                .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu1)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu1))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelMenu5.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu5.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu5.setRoundBottomLeft(10);
        panelMenu5.setRoundBottomRight(10);
        panelMenu5.setRoundTopLeft(10);
        panelMenu5.setRoundTopRight(10);

        addMenu5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu5ActionPerformed(evt);
            }
        });

        picMenu5.setBackground(new java.awt.Color(255, 255, 255));

        hargaMenu5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu5.setText("Rp 45.000");

        jLabel30.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel30.setText("Tambahkan");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel28.setText("Harga");

        qtyMenu5.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        labelMenu5.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu5.setText("Expresso");

        jLabel29.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel29.setText("Jumlah");

        javax.swing.GroupLayout panelMenu5Layout = new javax.swing.GroupLayout(panelMenu5);
        panelMenu5.setLayout(panelMenu5Layout);
        panelMenu5Layout.setHorizontalGroup(
            panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu5Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu5, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu5Layout.createSequentialGroup()
                        .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu5)
                            .addComponent(hargaMenu5, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(qtyMenu5))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelMenu5Layout.setVerticalGroup(
            panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu5Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu5, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(hargaMenu5))
                .addGap(8, 8, 8)
                .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu5)
                    .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu5))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelMenu6.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu6.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu6.setRoundBottomLeft(10);
        panelMenu6.setRoundBottomRight(10);
        panelMenu6.setRoundTopLeft(10);
        panelMenu6.setRoundTopRight(10);

        hargaMenu6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu6.setText("Rp 55.000");

        picMenu6.setBackground(new java.awt.Color(255, 255, 255));

        labelMenu6.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu6.setText("Lemon Cucumber");

        qtyMenu6.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        jLabel34.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel34.setText("Harga");

        jLabel36.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel36.setText("Tambahkan");

        addMenu6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu6ActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel35.setText("Jumlah");

        javax.swing.GroupLayout panelMenu6Layout = new javax.swing.GroupLayout(panelMenu6);
        panelMenu6.setLayout(panelMenu6Layout);
        panelMenu6Layout.setHorizontalGroup(
            panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenu6Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu6, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu6Layout.createSequentialGroup()
                        .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu6)
                            .addComponent(hargaMenu6, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(qtyMenu6))))
                .addGap(16, 16, 16))
        );
        panelMenu6Layout.setVerticalGroup(
            panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenu6Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu6, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(hargaMenu6))
                .addGap(8, 8, 8)
                .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu6)
                    .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu6))
                .addGap(18, 18, 18))
        );

        panelMenu2.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu2.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu2.setRoundBottomLeft(10);
        panelMenu2.setRoundBottomRight(10);
        panelMenu2.setRoundTopLeft(10);
        panelMenu2.setRoundTopRight(10);

        labelMenu2.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu2.setText("Chicken Combo ");

        addMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu2ActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Tambahkan");

        qtyMenu2.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Jumlah");

        hargaMenu2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu2.setText("Rp 108.000");

        picMenu2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Harga");

        javax.swing.GroupLayout panelMenu2Layout = new javax.swing.GroupLayout(panelMenu2);
        panelMenu2.setLayout(panelMenu2Layout);
        panelMenu2Layout.setHorizontalGroup(
            panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenu2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu2Layout.createSequentialGroup()
                        .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu2)
                            .addComponent(qtyMenu2)
                            .addComponent(hargaMenu2, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))))
                .addGap(15, 15, 15))
        );
        panelMenu2Layout.setVerticalGroup(
            panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(hargaMenu2))
                .addGap(8, 8, 8)
                .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu2)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu2))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelMenu7.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu7.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu7.setRoundBottomLeft(10);
        panelMenu7.setRoundBottomRight(10);
        panelMenu7.setRoundTopLeft(10);
        panelMenu7.setRoundTopRight(10);

        hargaMenu7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu7.setText("Rp 110.000");

        labelMenu7.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu7.setText("Apple Crumble");

        picMenu7.setBackground(new java.awt.Color(255, 255, 255));

        jLabel41.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel41.setText("Jumlah");

        jLabel40.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel40.setText("Harga");

        addMenu7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu7ActionPerformed(evt);
            }
        });

        jLabel42.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel42.setText("Tambahkan");

        qtyMenu7.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        javax.swing.GroupLayout panelMenu7Layout = new javax.swing.GroupLayout(panelMenu7);
        panelMenu7.setLayout(panelMenu7Layout);
        panelMenu7Layout.setHorizontalGroup(
            panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenu7Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu7, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu7Layout.createSequentialGroup()
                        .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu7)
                            .addComponent(hargaMenu7, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(qtyMenu7))))
                .addGap(16, 16, 16))
        );
        panelMenu7Layout.setVerticalGroup(
            panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenu7Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu7, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(hargaMenu7))
                .addGap(8, 8, 8)
                .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu7)
                    .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu7))
                .addGap(18, 18, 18))
        );

        panelMenu3.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu3.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu3.setRoundBottomLeft(10);
        panelMenu3.setRoundBottomRight(10);
        panelMenu3.setRoundTopLeft(10);
        panelMenu3.setRoundTopRight(10);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Harga");

        qtyMenu3.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        hargaMenu3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu3.setText("Rp 150.000");

        picMenu3.setBackground(new java.awt.Color(255, 255, 255));

        labelMenu3.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu3.setText("T-Bone Steak");

        addMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu3ActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Tambahkan");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Jumlah");

        javax.swing.GroupLayout panelMenu3Layout = new javax.swing.GroupLayout(panelMenu3);
        panelMenu3.setLayout(panelMenu3Layout);
        panelMenu3Layout.setHorizontalGroup(
            panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu3Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu3Layout.createSequentialGroup()
                        .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu3)
                            .addComponent(hargaMenu3, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(qtyMenu3))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelMenu3Layout.setVerticalGroup(
            panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(hargaMenu3))
                .addGap(8, 8, 8)
                .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu3)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu3))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelMenu4.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu4.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu4.setRoundBottomLeft(10);
        panelMenu4.setRoundBottomRight(10);
        panelMenu4.setRoundTopLeft(10);
        panelMenu4.setRoundTopRight(10);

        addMenu4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu4ActionPerformed(evt);
            }
        });

        labelMenu4.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu4.setText("Rib Eye Steak");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setText("Tambahkan");

        hargaMenu4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu4.setText("Rp 119.000");

        qtyMenu4.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel21.setText("Harga");

        picMenu4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel23.setText("Jumlah");

        javax.swing.GroupLayout panelMenu4Layout = new javax.swing.GroupLayout(panelMenu4);
        panelMenu4.setLayout(panelMenu4Layout);
        panelMenu4Layout.setHorizontalGroup(
            panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu4, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu4Layout.createSequentialGroup()
                        .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu4)
                            .addComponent(hargaMenu4, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(qtyMenu4))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelMenu4Layout.setVerticalGroup(
            panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu4, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(hargaMenu4))
                .addGap(8, 8, 8)
                .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu4)
                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu4))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelMenu8.setBackground(new java.awt.Color(255, 255, 255));
        panelMenu8.setPreferredSize(new java.awt.Dimension(225, 290));
        panelMenu8.setRoundBottomLeft(10);
        panelMenu8.setRoundBottomRight(10);
        panelMenu8.setRoundTopLeft(10);
        panelMenu8.setRoundTopRight(10);

        jLabel47.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel47.setText("Jumlah");

        labelMenu8.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        labelMenu8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMenu8.setText("Creme Crumblee");

        addMenu8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMenu8ActionPerformed(evt);
            }
        });

        jLabel46.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel46.setText("Harga");

        qtyMenu8.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        jLabel48.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel48.setText("Tambahkan");

        picMenu8.setBackground(new java.awt.Color(255, 255, 255));

        hargaMenu8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hargaMenu8.setText("Rp 130.000");

        javax.swing.GroupLayout panelMenu8Layout = new javax.swing.GroupLayout(panelMenu8);
        panelMenu8.setLayout(panelMenu8Layout);
        panelMenu8Layout.setHorizontalGroup(
            panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu8Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(picMenu8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelMenu8, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMenu8Layout.createSequentialGroup()
                        .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel46, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMenu8)
                            .addComponent(hargaMenu8, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(qtyMenu8))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelMenu8Layout.setVerticalGroup(
            panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenu8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(picMenu8, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMenu8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(hargaMenu8))
                .addGap(8, 8, 8)
                .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qtyMenu8)
                    .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(panelMenu8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMenu8))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelBGLayout = new javax.swing.GroupLayout(panelBG);
        panelBG.setLayout(panelBGLayout);
        panelBGLayout.setHorizontalGroup(
            panelBGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBGLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(panelBGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelBGLayout.createSequentialGroup()
                        .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(panelMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(panelMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(panelMenu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelBGLayout.createSequentialGroup()
                        .addComponent(panelMenu5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(panelMenu6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(panelMenu7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(panelMenu8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        panelBGLayout.setVerticalGroup(
            panelBGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBGLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(panelBGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMenu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addGroup(panelBGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMenu6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMenu5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMenu7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMenu8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1532, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelBG, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 719, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelBG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarActionPerformed
        int tunai = Integer.parseInt(inTunai.getText());
        kembali = tunai - total;

        if (tunai == 0) {
            JOptionPane.showMessageDialog(null, "Masukkan nominal tunai");
            return;
        } else if (kembali < 0) {
            JOptionPane.showMessageDialog(null, "Uang tunai tidak mencukupi");
            return;
        }

        String inputMeja = this.inNoMeja.getText().trim(); // Ambil input No Meja
        String namaPelanggan = this.inNamaCust.getText().trim(); // Ambil input Nama Pelanggan

        if (inputMeja.isEmpty() || namaPelanggan.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Harap lengkapi semua input (No Meja, Nama Pelanggan)!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Validasi Meja
            String queryMeja = "SELECT * FROM tb_meja WHERE id_meja = ?";
            PreparedStatement stmtMeja = conn.prepareStatement(queryMeja);
            stmtMeja.setString(1, inputMeja);

            ResultSet rsMeja = stmtMeja.executeQuery();

            if (rsMeja.next()) {
                int statusMeja = rsMeja.getInt("status");
                String namaMeja = rsMeja.getString("nama");

                if (statusMeja == 0) {
                    JOptionPane.showMessageDialog(null, "Meja tidak tersedia!");
                    return;
                }

                // Totalkan qty untuk semua pesanan
                int totalQty = 0;
                for (Map<String, Object> order : orders) {
                    totalQty += (int) order.get("qty");
                }

                // Format tanggal dan waktu untuk ID Transaksi
                SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmm");
                Date date = new Date(); // Tanggal dan waktu saat ini
                String idTransaksi = "TRX" + formatter.format(date); // Gabungkan prefix dengan tanggal-waktu

                // Membuat format tanggal dan waktu sesuai format 'yyyy-MM-dd HH:mm:ss' untuk tipe DATETIME
                SimpleDateFormat formatterDateTrx = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = formatterDateTrx.format(new Date());

                // Query SQL untuk insert ke tb_transaksi
                String queryInsertTransaksi = "INSERT INTO tb_transaksi (id_transaksi, id_produk, id_meja, nama_pel, tgl_transaksi, qty, subtotal, ppn, total_harga, tunai, kembalian, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement stmtInsertTransaksi = conn.prepareStatement(queryInsertTransaksi);

                for (Map<String, Object> order : orders) {
                    stmtInsertTransaksi.setString(1, idTransaksi);
                    stmtInsertTransaksi.setInt(2, (int) order.get("id_produk"));
                    stmtInsertTransaksi.setString(3, inputMeja);
                    stmtInsertTransaksi.setString(4, namaPelanggan);
                    stmtInsertTransaksi.setString(5, formattedDate);
                    stmtInsertTransaksi.setInt(6, (int) order.get("qty"));
                    stmtInsertTransaksi.setInt(7, subtotal);
                    stmtInsertTransaksi.setInt(8, PPN);
                    stmtInsertTransaksi.setInt(9, total);
                    stmtInsertTransaksi.setInt(10, tunai);
                    stmtInsertTransaksi.setInt(11, kembali);
                    stmtInsertTransaksi.setInt(12, 1); // Status pembayaran selesai
                    stmtInsertTransaksi.executeUpdate();
                }

                // Update status meja menjadi tidak tersedia
                String queryUpdateMeja = "UPDATE tb_meja SET status = 0 WHERE id_meja = ?";
                PreparedStatement stmtUpdateMeja = conn.prepareStatement(queryUpdateMeja);
                stmtUpdateMeja.setString(1, inputMeja);
                stmtUpdateMeja.executeUpdate();

                // Kurangi stok produk di database
                for (Map<String, Object> order : orders) {
                    String queryUpdateStok = "UPDATE tb_produk SET stok = ? WHERE id_produk = ?";
                    PreparedStatement stmtUpdateStok = conn.prepareStatement(queryUpdateStok);
                    stmtUpdateStok.setInt(1, (int) order.get("stok_akhir"));
                    stmtUpdateStok.setInt(2, (int) order.get("id_produk"));
                    stmtUpdateStok.executeUpdate();
                }

                // Tambahkan detail pelanggan dan meja ke outPesanan
                StringBuilder pesananText = new StringBuilder(outPesanan.getText());
                pesananText.append("\n\n************** Detail Transaksi *************\n")
                        .append("ID  : ").append(idTransaksi).append("\n")
                        .append("Nama: ").append(namaPelanggan).append("\n")
                        .append("Meja: ").append(inputMeja).append("\n")
                        .append("\nSubtotal : Rp ").append(String.format("%,d", subtotal))
                        .append("\nPajak 11%: Rp ").append(String.format("%,d", PPN))
                        .append("\nTotal    : Rp ").append(String.format("%,d", total))
                        .append("\nTunai    : Rp ").append(String.format("%,d", tunai))
                        .append("\nKembalian: Rp ").append(String.format("%,d", kembali))
                        .append("\n**************** Terima Kasih ***************\n");
                outPesanan.setText(pesananText.toString());

                JOptionPane.showMessageDialog(null, "Transaksi berhasil!");

                // Reset pesanan
                orders.clear();
                btnBayar.setEnabled(false);
                btnPrint.setEnabled(true);

            } else {
                JOptionPane.showMessageDialog(null, "Nomor meja tidak ditemukan!");
            }

            rsMeja.close();
            stmtMeja.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat memproses transaksi: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnBayarActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        try {
            JTextArea printTextArea = new JTextArea();
            printTextArea.setText(outPesanan.getText());
            printTextArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 10));
            printTextArea.print();
        } catch (PrinterException ex) {
            Logger.getLogger(MenuPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        reset();
    }//GEN-LAST:event_btnResetActionPerformed

    private void inTunaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inTunaiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inTunaiActionPerformed

    private void addMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu1ActionPerformed
        prosesOrder(qtyMenu1, addMenu1, labelMenu1, 103);
    }//GEN-LAST:event_addMenu1ActionPerformed

    private void addMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu2ActionPerformed
        prosesOrder(qtyMenu2, addMenu2, labelMenu2, 102);
    }//GEN-LAST:event_addMenu2ActionPerformed

    private void addMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu3ActionPerformed
        prosesOrder(qtyMenu3, addMenu3, labelMenu3, 101);
    }//GEN-LAST:event_addMenu3ActionPerformed

    private void addMenu4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu4ActionPerformed
        prosesOrder(qtyMenu4, addMenu4, labelMenu4, 100);
    }//GEN-LAST:event_addMenu4ActionPerformed

    private void addMenu5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu5ActionPerformed
        prosesOrder(qtyMenu5, addMenu5, labelMenu5, 51);
    }//GEN-LAST:event_addMenu5ActionPerformed

    private void addMenu6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu6ActionPerformed
        prosesOrder(qtyMenu6, addMenu6, labelMenu6, 52);
    }//GEN-LAST:event_addMenu6ActionPerformed

    private void addMenu7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu7ActionPerformed
        prosesOrder(qtyMenu7, addMenu7, labelMenu7, 12);
    }//GEN-LAST:event_addMenu7ActionPerformed

    private void addMenu8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenu8ActionPerformed
        prosesOrder(qtyMenu8, addMenu8, labelMenu8, 11);
    }//GEN-LAST:event_addMenu8ActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MenuPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MenuPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MenuPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MenuPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MenuPage().setVisible(true);

        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addMenu1;
    private javax.swing.JCheckBox addMenu2;
    private javax.swing.JCheckBox addMenu3;
    private javax.swing.JCheckBox addMenu4;
    private javax.swing.JCheckBox addMenu5;
    private javax.swing.JCheckBox addMenu6;
    private javax.swing.JCheckBox addMenu7;
    private javax.swing.JCheckBox addMenu8;
    private Custom.Button btnBayar;
    private Custom.Button btnPrint;
    private Custom.Button btnReset;
    private javax.swing.JLabel hargaMenu1;
    private javax.swing.JLabel hargaMenu2;
    private javax.swing.JLabel hargaMenu3;
    private javax.swing.JLabel hargaMenu4;
    private javax.swing.JLabel hargaMenu5;
    private javax.swing.JLabel hargaMenu6;
    private javax.swing.JLabel hargaMenu7;
    private javax.swing.JLabel hargaMenu8;
    private javax.swing.JTextField inNamaCust;
    private javax.swing.JTextField inNoMeja;
    private javax.swing.JTextField inTunai;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelMenu1;
    private javax.swing.JLabel labelMenu2;
    private javax.swing.JLabel labelMenu3;
    private javax.swing.JLabel labelMenu4;
    private javax.swing.JLabel labelMenu5;
    private javax.swing.JLabel labelMenu6;
    private javax.swing.JLabel labelMenu7;
    private javax.swing.JLabel labelMenu8;
    private javax.swing.JLabel outPajak;
    private javax.swing.JTextArea outPesanan;
    private javax.swing.JLabel outSubtotal;
    private javax.swing.JLabel outTotal;
    private Custom.Panel panelBG;
    private Custom.Panel panelMenu1;
    private Custom.Panel panelMenu2;
    private Custom.Panel panelMenu3;
    private Custom.Panel panelMenu4;
    private Custom.Panel panelMenu5;
    private Custom.Panel panelMenu6;
    private Custom.Panel panelMenu7;
    private Custom.Panel panelMenu8;
    private javax.swing.JLabel picMenu1;
    private javax.swing.JLabel picMenu2;
    private javax.swing.JLabel picMenu3;
    private javax.swing.JLabel picMenu4;
    private javax.swing.JLabel picMenu5;
    private javax.swing.JLabel picMenu6;
    private javax.swing.JLabel picMenu7;
    private javax.swing.JLabel picMenu8;
    private javax.swing.JSpinner qtyMenu1;
    private javax.swing.JSpinner qtyMenu2;
    private javax.swing.JSpinner qtyMenu3;
    private javax.swing.JSpinner qtyMenu4;
    private javax.swing.JSpinner qtyMenu5;
    private javax.swing.JSpinner qtyMenu6;
    private javax.swing.JSpinner qtyMenu7;
    private javax.swing.JSpinner qtyMenu8;
    private Custom.Panel rightPanel;
    private javax.swing.JLabel tanggalLabel;
    private Custom.Panel topPanel;
    private javax.swing.JLabel waktuLabel;
    // End of variables declaration//GEN-END:variables
}

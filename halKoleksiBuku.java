package perpustakaandigital;

import Config.koneksi;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.MessageFormat;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class halKoleksiBuku extends javax.swing.JFrame {
       
    Connection conn;
    Statement stmt;
    ResultSet rs;
    DefaultTableModel dtm;

    public halKoleksiBuku() {
        initComponents();
        this.setLocationRelativeTo(null);
        
        // Inisialisasi koneksi
        try {
            conn = new koneksi().getConnection();
            tampilTabel(); // Tampilkan data tabel
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi gagal: " + e.getMessage());
        }
        
        tfCariJudul.addCaretListener(evt -> {
          if (tfCariJudul.getText().trim().isEmpty()) {
            tampilTabel(); // Menampilkan semua data jika input kosong
            }
        });
        
        for (var listener : btnPinjam.getActionListeners()) {
            btnPinjam.removeActionListener(listener);
        }
        btnPinjam.addActionListener(evt -> simpanPeminjaman());
    }

    private DefaultTableModel buatTabelModel(ResultSet rs) throws SQLException {
        dtm = new DefaultTableModel();
        ResultSetMetaData rsmd = rs.getMetaData();
        int jmlKolom = rsmd.getColumnCount();

        // Menambahkan nama kolom ke tabel
        for (int i = 1; i <= jmlKolom; i++) {
            dtm.addColumn(rsmd.getColumnName(i));
        }

        // Menambahkan data ke tabel
        while (rs.next()) {
            Object[] rowData = new Object[jmlKolom];
            for (int i = 1; i <= jmlKolom; i++) {
                rowData[i - 1] = rs.getObject(i);
            }
            dtm.addRow(rowData);
        }
        return dtm;
    }

    private void tampilTabel() {
        String sql = "SELECT * FROM buku";
        try {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(sql);

            if (!rs.isBeforeFirst()) { // Mengecek apakah data kosong
                JOptionPane.showMessageDialog(null, "Data tidak ditemukan");
                return;
            }

            // Buat model tabel dan set ke JTable
            dtm = buatTabelModel(rs);
            lbTabelBuku.setModel(dtm);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }
    
    private void cariBuku() {
        String keyword = tfCariJudul.getText(); // Ambil teks dari JTextField

        // Jika kosong, tampilkan semua data
        if (keyword.trim().isEmpty()) {
            tampilTabel();
            return;
        }

        String sql = "SELECT * FROM buku WHERE Judul LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%"); // Gunakan wildcard untuk pencarian sebagian
            rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) { // Mengecek apakah hasil pencarian kosong
                JOptionPane.showMessageDialog(null, "Buku dengan judul \"" + keyword + "\" tidak ditemukan.");
                return;
            }

            // Buat model tabel baru dari hasil pencarian
            dtm = buatTabelModel(rs);
            lbTabelBuku.setModel(dtm); // Set model ke JTable utama
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error saat mencari buku: " + ex.getMessage());
        }
    }

    private void simpanPeminjaman() {
        String kodeBuku = tfKodeBuku.getText().trim();
        String judulBuku = tfJudulBuku.getText().trim();
        String tanggalPinjam = tfTanggalPinjam.getText().trim();

        // Validasi input
        if (kodeBuku.isEmpty() || judulBuku.isEmpty() || tanggalPinjam.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi format tanggal
        java.sql.Date sqlTanggalPinjam;
        try {
            java.util.Date parsedDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(tanggalPinjam);
            sqlTanggalPinjam = new java.sql.Date(parsedDate.getTime());
        } catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Cek apakah buku ada di tabel "buku"
        String cekBukuSql = "SELECT COUNT(*) FROM buku WHERE Kode_buku = ? AND Judul = ?";
        try (PreparedStatement cekStmt = conn.prepareStatement(cekBukuSql)) {
            cekStmt.setString(1, kodeBuku);
            cekStmt.setString(2, judulBuku);
            try (ResultSet rs = cekStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(this, "Buku tidak ditemukan di database. Tidak dapat dipinjam.", "Error", JOptionPane.ERROR_MESSAGE);
                    resetForm();
                    return;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saat memeriksa data buku: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cek apakah buku sudah dipinjam
        String cekPinjamSql = "SELECT COUNT(*) FROM buku_pinjam WHERE Kode_buku = ?";
        try (PreparedStatement cekPinjamStmt = conn.prepareStatement(cekPinjamSql)) {
            cekPinjamStmt.setString(1, kodeBuku);
            try (ResultSet rs = cekPinjamStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Buku \"" + judulBuku + "\" sudah dipinjam.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saat memeriksa data pinjaman: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simpan data ke tabel "buku_pinjam"
        String sql = "INSERT INTO buku_pinjam (Kode_buku, Judul,Tanggal_pinjam) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kodeBuku);
            pstmt.setString(2, judulBuku);
            pstmt.setDate(3, sqlTanggalPinjam);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Buku \"" + judulBuku + "\" telah dipinjam.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saat menyimpan data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        tfKodeBuku.setText("");
        tfJudulBuku.setText("");
        tfTanggalPinjam.setText("");
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        tfCariJudul = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lbTabelBuku = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tfKodeBuku = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        tfJudulBuku = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfTanggalPinjam = new javax.swing.JTextField();
        btnPinjam = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Serif", 1, 18)); // NOI18N
        jLabel1.setText("KOLEKSI BUKU");

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tfCariJudul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfCariJudulActionPerformed(evt);
            }
        });
        jPanel1.add(tfCariJudul, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 0, 190, -1));

        btnCari.setText("Cari");
        btnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCariActionPerformed(evt);
            }
        });
        jPanel1.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 0, 70, -1));

        jButton2.setText("Kembali");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 80, -1));

        lbTabelBuku.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(lbTabelBuku);

        jLabel2.setFont(new java.awt.Font("Serif", 1, 18)); // NOI18N
        jLabel2.setText("PEMINJAMAN BUKU");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Kode Buku");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Judul Buku");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Tanggal Peminjaman");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(151, 151, 151)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(54, 54, 54)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfKodeBuku)
                    .addComponent(tfJudulBuku)
                    .addComponent(tfTanggalPinjam, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
                .addContainerGap(74, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfKodeBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tfJudulBuku, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tfTanggalPinjam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        btnPinjam.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnPinjam.setText("Pinjam");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(250, 250, 250))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(225, 225, 225))))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPinjam)
                .addGap(284, 284, 284))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1)
                .addGap(29, 29, 29)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPinjam)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfCariJudulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCariJudulActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfCariJudulActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        new Menu().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        cariBuku();
    }//GEN-LAST:event_btnCariActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnPinjam;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable lbTabelBuku;
    private javax.swing.JTextField tfCariJudul;
    private javax.swing.JTextField tfJudulBuku;
    private javax.swing.JTextField tfKodeBuku;
    private javax.swing.JTextField tfTanggalPinjam;
    // End of variables declaration//GEN-END:variables
}

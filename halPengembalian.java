package perpustakaandigital;

import Config.koneksi;
import java.sql.*;
import javax.swing.JOptionPane;

public class halPengembalian extends javax.swing.JFrame {

    Connection conn;
    
    public halPengembalian() {
        initComponents();
        
        conn = new koneksi().getConnection();
        
        btnKembalikan.addActionListener(evt -> prosesPengembalian());
        
        this.setLocationRelativeTo(null);
    }
    
    
    private void prosesPengembalian() {
        String kodeBuku = tfKodeBuku.getText().trim();
        String judulBuku = tfJudulBuku.getText().trim();
        String tanggalPinjam = tfTanggalPinjam.getText().trim(); // Tanggal pinjam diinput manual
        String tanggalKembali = tfTanggalKembali.getText().trim(); // Tanggal pengembalian diinput manual

        // Validasi input
        if (kodeBuku.isEmpty() || judulBuku.isEmpty() || tanggalPinjam.isEmpty() || tanggalKembali.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi format tanggal
        java.sql.Date sqlTanggalPinjam, sqlTanggalKembali;
        try {
            java.util.Date parsedPinjam = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(tanggalPinjam);
            sqlTanggalPinjam = new java.sql.Date(parsedPinjam.getTime());

            java.util.Date parsedKembali = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(tanggalKembali);
            sqlTanggalKembali = new java.sql.Date(parsedKembali.getTime());
        } catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cek apakah data cocok di database
        String cekSql = "SELECT COUNT(*) FROM buku_pinjam WHERE Kode_buku = ? AND Judul = ? AND Tanggal_pinjam = ?";
        try (PreparedStatement cekStmt = conn.prepareStatement(cekSql)) {
            cekStmt.setString(1, kodeBuku);
            cekStmt.setString(2, judulBuku);
            cekStmt.setDate(3, sqlTanggalPinjam);

            try (ResultSet rs = cekStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(this, "Data buku tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saat memeriksa data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Hapus data dari database
        String hapusSql = "DELETE FROM buku_pinjam WHERE Kode_buku = ? AND Judul = ? AND Tanggal_pinjam = ?";
        try (PreparedStatement hapusStmt = conn.prepareStatement(hapusSql)) {
            hapusStmt.setString(1, kodeBuku);
            hapusStmt.setString(2, judulBuku);
            hapusStmt.setDate(3, sqlTanggalPinjam);

            hapusStmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Buku \"" + judulBuku + "\" telah berhasil dikembalikan.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saat menghapus data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        tfKodeBuku.setText("");
        tfJudulBuku.setText("");
        tfTanggalPinjam.setText("");
        tfTanggalKembali.setText("");
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfKodeBuku = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfJudulBuku = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        tfTanggalPinjam = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tfTanggalKembali = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        btnKembalikan = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("PENGEMBALIAN BUKU");
        jPanel2.add(jLabel6, new java.awt.GridBagConstraints());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Kode Buku");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 80, 20));

        tfKodeBuku.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKodeBukuActionPerformed(evt);
            }
        });
        jPanel1.add(tfKodeBuku, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 20, 300, -1));

        jLabel2.setText("Judul Buku");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 100, 20));

        tfJudulBuku.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfJudulBukuActionPerformed(evt);
            }
        });
        jPanel1.add(tfJudulBuku, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 50, 300, -1));

        jLabel4.setText("YYYY-MM-DD");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 80, 80, 20));

        tfTanggalPinjam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTanggalPinjamActionPerformed(evt);
            }
        });
        jPanel1.add(tfTanggalPinjam, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 80, 210, -1));

        jLabel5.setText("Tanggal Pinjam");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 110, 20));

        jLabel8.setText("Tanggal Pengembalian");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 140, 20));

        tfTanggalKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTanggalKembaliActionPerformed(evt);
            }
        });
        jPanel1.add(tfTanggalKembali, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 110, 210, -1));

        jLabel9.setText("YYYY-MM-DD");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 110, 80, 20));

        btnKembalikan.setText("Selesai");
        btnKembalikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembalikanActionPerformed(evt);
            }
        });

        jButton2.setText("Kembali");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Hapus");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnKembalikan)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnKembalikan)
                        .addComponent(jButton3))
                    .addComponent(jButton2))
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfKodeBukuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKodeBukuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKodeBukuActionPerformed

    private void tfJudulBukuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfJudulBukuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfJudulBukuActionPerformed

    private void tfTanggalPinjamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTanggalPinjamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTanggalPinjamActionPerformed

    private void tfTanggalKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTanggalKembaliActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTanggalKembaliActionPerformed

    private void btnKembalikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembalikanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnKembalikanActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        new Menu().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        resetForm();
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(halPengembalian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(halPengembalian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(halPengembalian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(halPengembalian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new halPengembalian().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKembalikan;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField tfJudulBuku;
    private javax.swing.JTextField tfKodeBuku;
    private javax.swing.JTextField tfTanggalKembali;
    private javax.swing.JTextField tfTanggalPinjam;
    // End of variables declaration//GEN-END:variables
}

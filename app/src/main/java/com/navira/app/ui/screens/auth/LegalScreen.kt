package com.navira.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navira.app.R
import com.navira.app.ui.components.CartoonCard
import com.navira.app.ui.theme.StickerBorder
import com.navira.app.ui.theme.SurfaceTint
import com.navira.app.ui.theme.SurfaceWhite
import com.navira.app.ui.theme.TextPrimary
import com.navira.app.ui.theme.TextSecondary

@Composable
fun LegalScreen(
    isTerms: Boolean,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    val title = if (isTerms) "Syarat & Ketentuan" else "Kebijakan Privasi"
    val content = if (isTerms) {
        """
        Selamat datang di Navira!

        1. Penerimaan Ketentuan
        Dengan mengunduh, mengakses, atau menggunakan aplikasi Navira, Anda menyetujui untuk terikat oleh Syarat dan Ketentuan ini.

        2. Penggunaan Layanan
        Navira adalah aplikasi pencatat perjalanan realtime berbasis GPS. Anda bertanggung jawab penuh atas keamanan fisik diri Anda saat menggunakan aplikasi di luar ruangan.

        3. Akun Pengguna
        Anda dapat menggunakan Navira dalam mode Tamu, Demo, atau Member. Akun Demo menyimpan data terbatas secara lokal. Pendaftaran akun Member memungkinkan pencadangan dan sinkronisasi rute perjalanan.

        4. Pembatasan Tanggung Jawab
        Navira berupaya memberikan akurasi data GPS dan perkiraan kalori sebaik mungkin, namun tidak menjamin 100% ketepatan mutlak pada segala kondisi sinyal atau medan lingkungan.
        """.trimIndent()
    } else {
        """
        Kebijakan Privasi Navira

        1. Pengumpulan Data Lokasi
        Navira mengumpulkan data lokasi presisi (GPS) dan lokasi latar belakang (background location) HANYA saat sesi tracking perjalanan sedang aktif secara sengaja oleh Anda.

        2. Penggunaan Data Lokasi
        Data lokasi digunakan murni untuk menggambar rute perjalanan realtime, menghitung jarak tempuh, mengoperasikan fitur Pulang ke Start, dan menyimpan riwayat trip Anda.

        3. Keamanan & Enkripsi
        Semua data lokal di perangkat dilindungi dengan standar keamanan tinggi. Data tidak pernah dijual kepada pihak ketiga atau pengiklan mana pun.

        4. Hak Pengguna & Penghapusan Data
        Anda memiliki hak penuh untuk mengekspor data perjalanan Anda dalam format GPX dan menghapus akun beserta seluruh data riwayat perjalanan secara permanen kapan saja melalui menu Profil.
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(SurfaceWhite, CircleShape)
                    .border(2.dp, StickerBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        CartoonCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

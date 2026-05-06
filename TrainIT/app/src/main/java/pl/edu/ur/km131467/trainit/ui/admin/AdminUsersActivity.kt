package pl.edu.ur.km131467.trainit.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import pl.edu.ur.km131467.trainit.R
import pl.edu.ur.km131467.trainit.data.local.SessionManager
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto
import pl.edu.ur.km131467.trainit.data.repository.AdminRepository

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val adminRepository = AdminRepository()

    private lateinit var rvUsers: RecyclerView
    private lateinit var tvEmpty: TextView

    private val adapter = UsersAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            finish()
            return
        }

        setContentView(R.layout.activity_admin_users)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvUsers = findViewById(R.id.rvUsers)
        tvEmpty = findViewById(R.id.tvEmpty)

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Brak tokena sesji", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val authHeader = "Bearer $token"

        lifecycleScope.launch {
            adminRepository.getUsers(authHeader)
                .onSuccess { users ->
                    adapter.submit(users)
                    tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                }
                .onFailure { error ->
                    adapter.submit(emptyList())
                    tvEmpty.visibility = View.VISIBLE
                    Toast.makeText(
                        this@AdminUsersActivity,
                        error.message ?: "Nie udało się pobrać użytkowników",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun showUserActions(user: UserDto) {
        val items = mutableListOf<String>()
        items.add("Zmień rolę")
        items.add(if (user.isActive) "Zablokuj" else "Odblokuj")
        items.add("Usuń użytkownika")

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("${user.firstName} ${user.lastName}")
            .setItems(items.toTypedArray()) { _, which ->
                when (which) {
                    0 -> showChangeRoleDialog(user)
                    1 -> if (user.isActive) blockUser(user) else unblockUser(user)
                    2 -> confirmDeleteUser(user)
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showChangeRoleDialog(user: UserDto) {
        val roles = arrayOf("USER", "TRAINER", "ADMIN")
        val currentIndex = roles.indexOf(user.role.uppercase()).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Zmień rolę")
            .setSingleChoiceItems(roles, currentIndex, null)
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Zmień") { dialog, _ ->
                val selected = (dialog as? androidx.appcompat.app.AlertDialog)
                    ?.listView
                    ?.checkedItemPosition
                    ?.takeIf { it in roles.indices }
                    ?.let { roles[it] }
                    ?: roles[currentIndex]
                changeRole(user, selected)
            }
            .show()
    }

    private fun changeRole(user: UserDto, newRole: String) {
        val authHeader = buildAuthHeader() ?: return
        lifecycleScope.launch {
            adminRepository.changeRole(authHeader, user.id, newRole)
                .onSuccess {
                    Toast.makeText(this@AdminUsersActivity, "Zmieniono rolę", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
                .onFailure {
                    Toast.makeText(
                        this@AdminUsersActivity,
                        it.message ?: "Nie udało się zmienić roli",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun blockUser(user: UserDto) {
        val authHeader = buildAuthHeader() ?: return
        lifecycleScope.launch {
            adminRepository.blockUser(authHeader, user.id)
                .onSuccess {
                    Toast.makeText(this@AdminUsersActivity, "Użytkownik zablokowany", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
                .onFailure {
                    Toast.makeText(
                        this@AdminUsersActivity,
                        it.message ?: "Nie udało się zablokować użytkownika",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun unblockUser(user: UserDto) {
        val authHeader = buildAuthHeader() ?: return
        lifecycleScope.launch {
            adminRepository.unblockUser(authHeader, user.id)
                .onSuccess {
                    Toast.makeText(this@AdminUsersActivity, "Użytkownik odblokowany", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
                .onFailure {
                    Toast.makeText(
                        this@AdminUsersActivity,
                        it.message ?: "Nie udało się odblokować użytkownika",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun confirmDeleteUser(user: UserDto) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_TrainIT_MaterialAlertDialog)
            .setTitle("Usuń użytkownika")
            .setMessage("Czy na pewno chcesz usunąć użytkownika ${user.firstName} ${user.lastName}?")
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Usuń") { _, _ ->
                deleteUser(user)
            }
            .show()
    }

    private fun deleteUser(user: UserDto) {
        val authHeader = buildAuthHeader() ?: return
        lifecycleScope.launch {
            adminRepository.deleteUser(authHeader, user.id)
                .onSuccess {
                    Toast.makeText(this@AdminUsersActivity, "Użytkownik usunięty", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
                .onFailure {
                    Toast.makeText(
                        this@AdminUsersActivity,
                        it.message ?: "Nie udało się usunąć użytkownika",
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    private fun buildAuthHeader(): String? {
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Brak tokena sesji", Toast.LENGTH_SHORT).show()
            finish()
            return null
        }
        return "Bearer $token"
    }

    private inner class UsersAdapter : RecyclerView.Adapter<UserViewHolder>() {
        private var items: List<UserDto> = emptyList()

        fun submit(newItems: List<UserDto>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_admin, parent, false)
            return UserViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(items[position])
        }
    }

    private inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvRoleBadge: TextView = itemView.findViewById(R.id.tvRoleBadge)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(user: UserDto) {
            tvName.text = "${user.firstName} ${user.lastName}".trim()
            tvEmail.text = user.email

            val role = user.role.uppercase()
            tvRoleBadge.text = role
            when (role) {
                "ADMIN" -> tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_hard)
                "TRAINER" -> tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_medium)
                else -> tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_easy)
            }

            if (user.isActive) {
                tvStatus.text = "Aktywny"
                tvStatus.setTextColor(getColor(R.color.stat_badge_green))
            } else {
                tvStatus.text = "Zablokowany"
                tvStatus.setTextColor(getColor(R.color.logout_red))
            }

            itemView.setOnLongClickListener {
                showUserActions(user)
                true
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, AdminUsersActivity::class.java)
    }
}


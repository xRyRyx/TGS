package com.tgs.app.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tgs.app.data.model.Contact
import com.tgs.app.databinding.ItemContactBinding

class ContactsAdapter(
    private val contacts: MutableList<Contact>,
    private val onEditClick: (Contact) -> Unit,
    private val onDelete: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact, onEditClick: (Contact) -> Unit, onDelete: (Contact) -> Unit) {
            binding.tvName.text = contact.name
            binding.tvPhone.text = contact.phonenumber

            binding.editBtn.setOnClickListener {
                onEditClick(contact) // Open edit activity
            }

            binding.deleteBtn.setOnClickListener {
                onDelete(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position], onEditClick, onDelete)
    }

    override fun getItemCount(): Int = contacts.size
}

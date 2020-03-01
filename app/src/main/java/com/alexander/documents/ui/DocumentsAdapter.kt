package com.alexander.documents.ui

import android.annotation.SuppressLint
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.document_item.view.*
import java.util.*
import java.text.SimpleDateFormat
import androidx.appcompat.widget.PopupMenu
import com.alexander.documents.R
import com.alexander.documents.entity.Document

/**
 * author alex
 */
class DocumentsAdapter(
    val documentClickListener: (Document) -> Unit,
    val popUpClickListener: (Int, Int, Int) -> Unit
) : ListAdapter<Document, RecyclerView.ViewHolder>(DiffCallback()) {

    var documents: MutableList<Document> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.document_item, parent, false)
        return DocumentsViewHolder(view)
    }

    override fun getItemCount(): Int = documents.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val document = documents[position]
        with(holder.itemView) {
            setOnClickListener { documentClickListener(document) }

            titleView.text = document.title
            initPopUpMenu(threePointsView, holder.adapterPosition)

            val date = Date(document.date * MS_TO_S)
            val dateFormat = SimpleDateFormat(PATTERN, Locale.getDefault())

            dateView.text =
                document.ext + " \u00B7 " + dateFormat.format(date) + " \u00B7 " + document.size
            typeView.text = document.type
            typeView.visibility = if (document.type.isEmpty()) View.GONE else View.VISIBLE
            setImage(document, photoView)
        }
    }

    fun deleteDocument(position: Int) {
        documents.removeAt(position)
        notifyItemRemoved(position)
    }

    fun renameDocument(documentTitle: String, position: Int) {
        documents[position].title = documentTitle
        notifyItemChanged(position)
    }

    private fun initPopUpMenu(view: View, position: Int) {
        view.setOnClickListener {
            val popup = PopupMenu(view.context, it, Gravity.CENTER, 0,
                R.style.PopupMenu
            )
            popup.inflate(R.menu.pop_up_menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.rename -> {
                            popUpClickListener(
                                documents[position].id,
                                MainActivity.RENAME_ACTION,
                                position
                            )
                            return true
                        }
                        R.id.delete -> {
                            popUpClickListener(
                                documents[position].id,
                                MainActivity.DELETE_ACTION,
                                position
                            )
                            return true
                        }
                    }
                    return false
                }
            })
            popup.show()
        }
    }

    private fun setImage(document: Document, photoView: ImageView) {
        if (document.photo != null) {
            Glide.with(photoView.context)
                .load(document.photo.src)
                .apply(
                    RequestOptions().transforms(
                        CenterCrop(),
                        RoundedCorners(6)
                    )
                )
                .into(photoView)
        } else {
            Glide.with(photoView.context)
                .load(getImageDrawable(document.ext))
                .apply(
                    RequestOptions().transforms(
                        CenterCrop(),
                        RoundedCorners(6)
                    )
                )
                .into(photoView)
        }
    }

    private fun getImageDrawable(ext: String): Int {
        return when (ext) {
            "ZIP" -> R.drawable.ic_placeholder_document_archive_72
            "MP4" -> R.drawable.ic_placeholder_document_video_72
            "MP3" -> R.drawable.ic_placeholder_document_music_72
            "PDF", "EPUB" -> R.drawable.ic_placeholder_document_book_72
            "TXT", "DOCX" -> R.drawable.ic_placeholder_document_text_72
            else -> R.drawable.ic_placeholder_document_other_72
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem == newItem
        }
    }

    class DocumentsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer

    private companion object {
        const val PATTERN: String = "dd MMMM yyyy"
        const val MS_TO_S: Int = 1000
    }
}
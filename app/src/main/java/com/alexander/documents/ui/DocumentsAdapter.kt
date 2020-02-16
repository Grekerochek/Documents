package com.alexander.documents.ui

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
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
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.content.Context
import kotlin.math.roundToInt
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
            photoView.setImageDrawable(null)
            photoView.background = null
            photoView.setPadding(0, 0, 0, 0)
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
            val imageDrawableAndColor = getImageDrawableAndColor(document.ext)
            photoView.setImageResource(imageDrawableAndColor.first)
            photoView.setBackgroundResource(R.drawable.round_corners_drawable)
            (photoView.background as GradientDrawable)
                .setColor(ContextCompat.getColor(photoView.context, imageDrawableAndColor.second))
            val dp = dpToPx(photoView.context, 25)
            photoView.setPadding(dp, dp, dp, dp)
        }
    }

    private fun getImageDrawableAndColor(ext: String): Pair<Int, Int> {
        return when (ext) {
            "ZIP" -> Pair(
                R.drawable.ic_zip,
                R.color.colorZip
            )
            "MP4" -> Pair(
                R.drawable.ic_video,
                R.color.colorVideo
            )
            "MP3" -> Pair(
                R.drawable.ic_music,
                R.color.colorMusic
            )
            "PDF", "EPUB" -> Pair(
                R.drawable.ic_book,
                R.color.colorBook
            )
            "TXT", "DOCX" -> Pair(
                R.drawable.ic_text,
                R.color.colorText
            )
            else -> Pair(
                R.drawable.ic_other,
                R.color.colorOther
            )
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
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
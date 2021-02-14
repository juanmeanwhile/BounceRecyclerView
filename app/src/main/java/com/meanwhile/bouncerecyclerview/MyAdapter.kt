package com.meanwhile.bouncerecyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter: RecyclerView.Adapter<MyAdapter.MyHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = 20

    class MyHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val textView = itemView.findViewById<TextView>(R.id.textView)

        fun bind(position: Int){
            textView.text = "Item $position"
        }
    }

}
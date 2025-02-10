package com.au.jobstudy.checkwith.pic

import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.BuildConfig
import com.au.jobstudy.R
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.databinding.HolderPartialPictureBinding
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.glide.glideSetAny
import com.au.module_android.permissions.systemTakePictureForResult
import com.au.module_android.utils.invisible
import com.au.module_android.utils.visible
import java.io.File

class CheckParentPicSelector(private val f:CheckPicturePartialFragment) {
    fun createAdapter() : PicAdapter{
        return PicAdapter { bean, pos, clickOnDelete ->
            if (clickOnDelete) {
                removeItem(pos)
            }

            if (bean.isAdd) {
                clickOnAdd(pos)
            }
        }
    }

    private fun removeItem(pos:Int) {
        f.adapter.removeItem(pos)
        markupAddItem()
    }

    private fun markupAddItem() {
        if (f.adapter.datas.size == (f.checkMode?.max ?: 1)) {
            return
        }
        val hasAddItem = f.adapter.datas.filter { it.isAdd }.map { it.isAdd }
        if (hasAddItem.isEmpty()) {
            f.adapter.addItem(Bean.ADD_BEAN)
        }
    }

    val launcher = f.systemTakePictureForResult()

    fun clickOnAdd(addIconPosition:Int) {
        val picture = File(Globals.goodCacheDir.path + "/pictures/" + CheckConsts.currentDay())
        picture.mkdirs()
        val file = File(picture, "pic_" + WeekDateUtil.currentHHmmssSSS() + ".png")
        val uri = FileProvider.getUriForFile(
            Globals.app,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )

        f.tempFiles.add(file)

        launcher.start(uri) { suc->
            if (suc) {
                val bean = Bean().also { it.file = file }
                if (addIconPosition >= (f.checkMode?.max ?: 1) - 1) {
                    f.adapter.updateItem(addIconPosition, bean)
                } else {
                    f.adapter.addItem(bean, addIconPosition)
                }

                markupAddItem()
            }
        }
    }
}

class PicAdapter(val itemClick:(bean:Bean, position:Int, clickOnDelete:Boolean)->Unit) : BindRcvAdapter<Bean, FeedBackPicHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedBackPicHolder {
        return FeedBackPicHolder(create(parent), itemClick)
    }

    override fun onBindViewHolder(holder: FeedBackPicHolder, position: Int) {
        holder.bindData(datas[position])
    }
}

class FeedBackPicHolder(binding: HolderPartialPictureBinding, itemClick:(bean:Bean, position:Int, clickOnDelete:Boolean)->Unit)
    : BindViewHolder<Bean, HolderPartialPictureBinding>(binding) {
    init {
        binding.root.onClick {
            currentData?.let{
                itemClick(it, bindingAdapterPosition, false)
            }
        }
        binding.ivDelete.onClick {
            currentData?.let{
                itemClick(it, bindingAdapterPosition, true)
            }
        }
    }

    override fun bindData(bean: Bean) {
        super.bindData(bean)
        if (bean.isAdd) {
            binding.ivPic.scaleType = ImageView.ScaleType.FIT_XY
            binding.ivPic.setImageResource(R.drawable.holder_pic_add)
            binding.ivDelete.invisible()
        } else {
            binding.ivPic.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivPic.glideSetAny(bean.file)
            binding.ivDelete.visible()
        }
    }
}
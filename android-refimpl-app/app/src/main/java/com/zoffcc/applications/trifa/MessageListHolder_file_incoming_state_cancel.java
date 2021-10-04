/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.net.URLConnection;

import androidx.recyclerview.widget.RecyclerView;
import info.guardianproject.iocipher.File;

import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.PREF__allow_open_encrypted_file_via_intent;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.PREF__global_font_size;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MessageListActivity.onClick_message_helper;
import static com.zoffcc.applications.trifa.MessageListActivity.onLongClick_message_helper;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE_FT_NORMAL;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListHolder_file_incoming_state_cancel extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message_;
    private Context context;

    ImageButton button_ok;
    ImageButton button_cancel;
    com.daimajia.numberprogressbar.NumberProgressBar ft_progressbar;
    ViewGroup ft_preview_container;
    ViewGroup ft_export_button_container;
    ViewGroup ft_buttons_container;
    ImageButton ft_preview_image;
    ImageButton ft_export_button;
    EmojiTextViewLinks textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    TextView date_time;
    ViewGroup layout_message_container;
    boolean is_selected = false;
    TextView message_text_date_string;
    ViewGroup message_text_date;

    public MessageListHolder_file_incoming_state_cancel(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        button_ok = (ImageButton) itemView.findViewById(R.id.ft_button_ok);
        button_cancel = (ImageButton) itemView.findViewById(R.id.ft_button_cancel);
        ft_progressbar = (com.daimajia.numberprogressbar.NumberProgressBar) itemView.findViewById(R.id.ft_progressbar);
        ft_preview_container = (ViewGroup) itemView.findViewById(R.id.ft_preview_container);
        ft_buttons_container = (ViewGroup) itemView.findViewById(R.id.ft_buttons_container);
        ft_preview_image = (ImageButton) itemView.findViewById(R.id.ft_preview_image);
        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        date_time = (TextView) itemView.findViewById(R.id.date_time);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
        message_text_date_string = (TextView) itemView.findViewById(R.id.message_text_date_string);
        message_text_date = (ViewGroup) itemView.findViewById(R.id.message_text_date);
        ft_export_button_container = (ViewGroup) itemView.findViewById(R.id.ft_export_button_container);
        ft_export_button = (ImageButton) itemView.findViewById(R.id.ft_export_button);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bindMessageList(Message m)
    {
        // Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new Message();
        }

        message_ = m;

        is_selected = false;
        if (selected_messages.isEmpty())
        {
            is_selected = false;
        }
        else
        {
            is_selected = selected_messages.contains(m.id);
        }

        if (is_selected)
        {
            layout_message_container.setBackgroundColor(Color.GRAY);
        }
        else
        {
            layout_message_container.setBackgroundColor(Color.TRANSPARENT);
        }

        // ft_preview_container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 150));
        // ft_preview_image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 150));

        resize_viewgroup(ft_preview_container, 150);
        resize_view(ft_preview_image, 150);

        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        message_text_date.setVisibility(View.GONE);
        int my_position = this.getAdapterPosition();
        if (my_position != RecyclerView.NO_POSITION)
        {
            if (MainActivity.message_list_fragment != null)
            {
                if (MainActivity.message_list_fragment.adapter != null)
                {
                    if (my_position < 1)
                    {
                        message_text_date_string.setText(
                                MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                        message_text_date.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if (!MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position).equals(
                                MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position - 1)))
                        {
                            message_text_date_string.setText(
                                    MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                            message_text_date.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------


        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        layout_message_container.setOnClickListener(onclick_listener);
        layout_message_container.setOnLongClickListener(onlongclick_listener);

        date_time.setText(long_date_time_format(m.rcvd_timestamp));

        final Message message = m;

        textView.addAutoLinkMode(AutoLinkMode.MODE_URL, AutoLinkMode.MODE_EMAIL, AutoLinkMode.MODE_HASHTAG,
                                 AutoLinkMode.MODE_MENTION);

        button_ok.setVisibility(View.GONE);
        button_cancel.setVisibility(View.GONE);
        ft_progressbar.setVisibility(View.GONE);
        ft_buttons_container.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);

        final Message message2 = message;

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);

        if (message.filedb_id == -1)
        {
            textView.setAutoLinkText("" + message.text + "\n *canceled*");
            if (MESSAGE_TEXT_SIZE[PREF__global_font_size] > MESSAGE_TEXT_SIZE_FT_NORMAL)
            {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE_FT_NORMAL);
            }

            ft_preview_image.setImageDrawable(null);
            ft_preview_container.setVisibility(View.GONE);
            ft_preview_image.setVisibility(View.GONE);
            ft_export_button_container.setVisibility(View.GONE);
            ft_export_button.setVisibility(View.GONE);
        }
        else
        {
            textView.setAutoLinkText("" + message.text + "\n OK");
            if (MESSAGE_TEXT_SIZE[PREF__global_font_size] > MESSAGE_TEXT_SIZE_FT_NORMAL)
            {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE_FT_NORMAL);
            }

            boolean is_image = false;
            boolean is_video = false;
            try
            {
                String mimeType = URLConnection.guessContentTypeFromName(message.filename_fullpath.toLowerCase());
                if (mimeType.startsWith("image/"))
                {
                    is_image = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (!is_image)
            {
                try
                {
                    String mimeType = URLConnection.guessContentTypeFromName(message.filename_fullpath.toLowerCase());
                    if (mimeType.startsWith("video/"))
                    {
                        is_video = true;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Log.i(TAG, "getView:033:STATE:CANCEL:OK:is_image=" + is_image);

            if (is_image)
            {

                //                final Drawable d3 = new IconicsDrawable(this.context).
                //                        icon(GoogleMaterial.Icon.gmd_photo).
                //                        backgroundColor(Color.TRANSPARENT).
                //                        color(Color.parseColor("#AA000000")).sizeDp(50);

                // ft_preview_image.setImageDrawable(d3);
                ft_preview_image.setImageResource(R.drawable.round_loading_animation);
                // final ImageButton ft_preview_image_ = ft_preview_image;

                if (PREF__compact_chatlist)
                {
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                }
                else
                {
                    textView.setVisibility(View.VISIBLE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);
                }

                if (VFS_ENCRYPT)
                {
                    ft_preview_image.setOnTouchListener(new View.OnTouchListener()
                    {
                        @Override
                        public boolean onTouch(View v, MotionEvent event)
                        {
                            if (event.getAction() == MotionEvent.ACTION_UP)
                            {
                                try
                                {
                                    Intent intent = new Intent(v.getContext(), ImageviewerActivity.class);
                                    intent.putExtra("image_filename", message2.filename_fullpath);
                                    v.getContext().startActivity(intent);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "open_attachment_intent:EE:" + e.getMessage());
                                }
                            }
                            else
                            {
                            }
                            return true;
                        }
                    });


                    info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(
                            message2.filename_fullpath);
                    try
                    {
                        // Log.i(TAG, "glide:img:001");

                        final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(
                                new RoundedCorners((int) dp2px(20)));
                        // apply(glide_options).

                        // loadImageFromUri(context, Uri.fromFile(new File(message2.filename_fullpath)), ft_preview_image,
                        //                  true);
                        GlideApp.
                                with(context).
                                load(f2).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                priority(Priority.LOW).
                                placeholder(R.drawable.round_loading_animation).
                                into(ft_preview_image);
                        // Log.i(TAG, "glide:img:002");

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if (is_video)  // ---- a video ----
            {
                try
                {
                    final Drawable d4 = new IconicsDrawable(context).
                            icon(GoogleMaterial.Icon.gmd_ondemand_video).
                            backgroundColor(Color.TRANSPARENT).
                            color(Color.parseColor("#AA000000")).sizeDp(50);

                    if (1 == 2 - 0)
                    {
                        info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(
                                message2.filename_fullpath);

                        GlideApp.
                                with(context).
                                load(f2).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                priority(Priority.LOW).
                                placeholder(R.drawable.round_loading_animation).
                                error(d4).
                                into(ft_preview_image);
                    }
                    else
                    {
                        resize_viewgroup(ft_preview_container, 60);
                        resize_view(ft_preview_image, 60);

                        GlideApp.
                                with(context).
                                load(d4).
                                diskCacheStrategy(DiskCacheStrategy.NONE).
                                skipMemoryCache(false).
                                priority(Priority.LOW).
                                into(ft_preview_image);
                    }
                    ft_preview_image.setOnTouchListener(null);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else // ---- not an image ----
            {
                final Drawable d3 = new IconicsDrawable(this.context).
                        icon(GoogleMaterial.Icon.gmd_attachment).
                        backgroundColor(Color.TRANSPARENT).
                        color(Color.parseColor("#AA000000")).sizeDp(50);

                resize_viewgroup(ft_preview_container, 60);
                resize_view(ft_preview_image, 60);

                // ft_preview_image.setImageDrawable(d3);
                GlideApp.
                        with(context).
                        load(d3).
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(false).
                        priority(Priority.LOW).
                        placeholder(R.drawable.round_loading_animation).
                        into(ft_preview_image);

                if (PREF__allow_open_encrypted_file_via_intent)
                {
                    ft_preview_image.setOnTouchListener(new View.OnTouchListener()
                    {
                        @Override
                        public boolean onTouch(View v, MotionEvent event)
                        {
                            if (event.getAction() == MotionEvent.ACTION_UP)
                            {
                                try
                                {
                                    final Uri uri = Uri.parse(
                                            IOCipherContentProvider.FILES_URI + message2.filename_fullpath);

                                    // Log.i(TAG, "view_file:" + IOCipherContentProvider.FILES_URI +
                                    //            message2.filename_fullpath);

                                    File file = new File(message2.filename_fullpath);
                                    String filename_without_path = file.getName();

                                    new AlertDialog.Builder(v.getContext()).setIcon(R.mipmap.ic_launcher).
                                            setTitle(filename_without_path).
                                            setNeutralButton("View", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    try
                                                    {
                                                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                                                        v.getContext().startActivity(sendIntent);
                                                    }
                                                    catch (ActivityNotFoundException e)
                                                    {
                                                        Log.e(TAG, "No relevant Activity found", e);
                                                    }
                                                }
                                            }).show();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "open_attachment_intent:EE:" + e.getMessage());
                                }
                            }
                            else
                            {
                            }
                            return true;
                        }
                    });
                }
                else
                {
                    ft_preview_image.setOnTouchListener(null);
                }
            }

            ft_export_button_container.setVisibility(View.VISIBLE);
            ft_export_button.setVisibility(View.VISIBLE);

            ft_export_button.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        try
                        {
                            new AlertDialog.Builder(v.getContext()).setIcon(R.mipmap.ic_launcher).
                                    setTitle("Export File to unencrypted Storage?").
                                    setCancelable(true).
                                    setNeutralButton("Export", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            try
                                            {
                                                final String export_filename =
                                                        SD_CARD_FILES_EXPORT_DIR + "/" + message2.tox_friendpubkey +
                                                        "/";
                                                final FileDB file_ = orma.selectFromFileDB().idEq(
                                                        message2.filedb_id).get(0);
                                                ProgressDialog progressDialog2 = null;

                                                try
                                                {
                                                    progressDialog2 = ProgressDialog.show(v.getContext(), "",
                                                                                          "exporting File ...");
                                                    progressDialog2.setCanceledOnTouchOutside(false);
                                                    progressDialog2.setOnCancelListener(
                                                            new DialogInterface.OnCancelListener()
                                                            {
                                                                @Override
                                                                public void onCancel(DialogInterface dialog)
                                                                {
                                                                }
                                                            });
                                                }
                                                catch (Exception e3)
                                                {
                                                    e3.printStackTrace();
                                                    Log.i(TAG, "save_selected_messages:EE1:" + e3.getMessage());
                                                }

                                                new MainActivity.save_selected_message_custom_asynchtask(v.getContext(),
                                                                                                         progressDialog2,
                                                                                                         file_,
                                                                                                         export_filename).execute();
                                            }
                                            catch (Exception e)
                                            {
                                            }
                                        }
                                    }).
                                    setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).
                                    show();
                        }
                        catch (Exception e)
                        {
                        }
                    }
                    return true;
                }
            });

            ft_preview_container.setVisibility(View.VISIBLE);
            ft_preview_image.setVisibility(View.VISIBLE);
        }

        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(
                context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                FriendList fl = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).get(0);

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    if (f1.length() > 0)
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(context).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                signature(new com.bumptech.glide.signature.StringSignatureZ(
                                        "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                        fl.avatar_update_timestamp)).
                                skipMemoryCache(false).
                                apply(glide_options).
                                priority(Priority.HIGH).
                                into(img_avatar);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v)
    {
        //  Log.i(TAG, "onClick");
    }

    @Override
    public boolean onLongClick(final View v)
    {
        // Log.i(TAG, "onLongClick");
        return true;
    }

    private View.OnClickListener onclick_listener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            is_selected = onClick_message_helper(v, is_selected, message_);
        }
    };

    private View.OnLongClickListener onlongclick_listener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(final View v)
        {
            MessageListActivity.long_click_message_return res = onLongClick_message_helper(context, v, is_selected,
                                                                                           message_);
            is_selected = res.is_selected;
            return res.ret_value;
        }
    };

    private void resize_viewgroup(ViewGroup vg, int height_in_dp)
    {
        try
        {
            float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height_in_dp,
                                                     vg.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) vg.getLayoutParams();
            if (params3.height != (int) pixels)
            {
                params3.height = (int) pixels;
                vg.setLayoutParams(params3);
            }
        }
        catch (Exception e)
        {
        }
    }

    private void resize_view(View vg, int height_in_dp)
    {
        try
        {
            float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height_in_dp,
                                                     vg.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) vg.getLayoutParams();
            if (params3.height != (int) pixels)
            {
                params3.height = (int) pixels;
                vg.setLayoutParams(params3);
            }
        }
        catch (Exception e)
        {
        }
    }
}

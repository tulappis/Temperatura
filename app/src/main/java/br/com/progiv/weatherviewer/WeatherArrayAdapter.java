package br.com.progiv.weatherviewer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    }

    //armazenar Bitmaps já baixados para reutilização:
    private Map<String, Bitmap> bitmaps = new HashMap<>();

    //contrutor
    public WeatherArrayAdapter(Context context, List<Weather> forecast){
        super(context, -1, forecast);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //obtém objeto Weather para esta posição de ListView especificada
        Weather day = getItem(position);
        ViewHolder viewHolder; //objeto que referencia as views do item da lista:
        //Verifica se há ViewHolder reutilizável de um item de ListView que
        // rolou para fora da tela; caso contrário, cria um viewholder
        if(convertView == null){
            //nenhu ViewHholder
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView = (TextView)convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView = (TextView)convertView.findViewById(R.id.humidityTextView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        //se o ícone de condição climática já foi baixado, o utiliza, caso contrário faz o download:
        if(bitmaps.containsKey(day.iconURL)){
            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
        }else{
            //download:
            new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }

        //obter outros dados do objeto Weather e colocar na view:
        Context context = getContext();
        viewHolder.dayTextView.setText(context.getString(R.string.day_description, day.dayOfWeek, day.description));
        viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));
        viewHolder.hiTextView.setText(context.getString(R.string.high_temp, day.maxTemp));
        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));
        return convertView;
    }

    //AsyncTask para carregar ícones de condição climática em uma thread separada
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{
        private ImageView imageView; //exibe a miniatura
        public LoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        //Carrega a imagem; string[0] é a string da URL que representa a imagem
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            try{
                URL url = new URL(strings[0]);//cria a URL para a imagem

                //abrir a conexão no HttpURLConnection, obter seu InputStream e baixar a imagem:
                connection = (HttpURLConnection) url.openConnection();
                try(InputStream inputStream = connection.getInputStream()){
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(strings[0], bitmap); //coloca em cacheh para uso posterior
                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                connection.disconnect();
            }
            return bitmap;
        }

        //configurar a imagem da condição climática no item da lista:
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}

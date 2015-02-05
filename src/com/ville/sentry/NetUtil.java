package com.ville.sentry;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import android.text.TextUtils;

public class NetUtil {

    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;

    private static final int UPLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int UPLOAD_READ_TIMEOUT = 5 * 60 * 1000;
	private static final String TAG = "HTTP";

	public static String doGet(String urlStr, Map<String, String> param) {
    	HttpURLConnection urlConn = null;
    	if(param != null){
    		StringBuilder sb = new StringBuilder(urlStr);
    		sb.append("?").append(encodeUrl(param));
    		urlStr = sb.toString();
    	}
        try {
        	URL url = new URL(urlStr);
            AppLog.d(TAG, "[doGet] " + url);
            urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setRequestMethod("GET");
            urlConn.setDoOutput(false);
            urlConn.setConnectTimeout(CONNECT_TIMEOUT);
            urlConn.setReadTimeout(READ_TIMEOUT);
            urlConn.setRequestProperty("Connection", "Keep-Alive");
            urlConn.setRequestProperty("Charset", "UTF-8");
            urlConn.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConn.connect();

            return handleResponse(urlConn);
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, e.getMessage());
        } finally {
        	if(urlConn != null) {
        		urlConn.disconnect();
        	}
        }
        return null;
    }
	
    public static String doPost(String urlAddress, Map<String, String> param) {
    	AppLog.d(TAG, "[doPost] " + urlAddress);
    	AppLog.d(TAG, "[doPost-param] " + param);
    	HttpURLConnection urlConn = null;
        try {
            URL url = new URL(urlAddress);
            urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setUseCaches(false);
            urlConn.setConnectTimeout(CONNECT_TIMEOUT);
            urlConn.setReadTimeout(READ_TIMEOUT);
            urlConn.setInstanceFollowRedirects(false);
            urlConn.setRequestProperty("Connection", "Keep-Alive");
            urlConn.setRequestProperty("Charset", "UTF-8");
            urlConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConn.connect();
            
            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
            out.write(encodeUrl(param).getBytes());
            out.flush();
            out.close();
            return handleResponse(urlConn);
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, e.getMessage());
        } finally {
        	if(urlConn != null){
        		urlConn.disconnect();
        	}
        }
        return null;
    }

    private static String handleResponse(HttpURLConnection httpURLConnection) throws Exception{
        int status = 0;
        status = httpURLConnection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
        	throw new IOException("getResponseCode Error");
        }
        return readResult(httpURLConnection);

    }

    private static String readResult(HttpURLConnection urlConn) throws Exception {
        InputStream is = null;
        BufferedReader buffer = null;
        try {
            is = urlConn.getInputStream();
            String encoding = urlConn.getContentEncoding();

            if (null != encoding && !"".equals(encoding) && encoding.equals("gzip")) {
                is = new GZIPInputStream(is);
            }

            buffer = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }
            AppLog.d(TAG, "[result]=" + sb.toString());
            return sb.toString();
        } catch (IOException e) {
            throw e;
        } finally {
        	is.close();
        	buffer.close();
        }

    }


//    public boolean doGetSaveFile(String urlStr, String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {
//
//        File file = FileManager.createNewFileInSDCard(path);
//        if (file == null) {
//            return false;
//        }
//
//        BufferedOutputStream out = null;
//        InputStream in = null;
//        HttpURLConnection urlConnection = null;
//        try {
//
//            URL url = new URL(urlStr);
//            AppLogger.d("download request=" + urlStr);
//            urlConnection = (HttpURLConnection) url.openConnection();
//
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setDoOutput(false);
//            urlConnection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT);
//            urlConnection.setReadTimeout(DOWNLOAD_READ_TIMEOUT);
//            urlConnection.setRequestProperty("Connection", "Keep-Alive");
//            urlConnection.setRequestProperty("Charset", "UTF-8");
//            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
//
//            urlConnection.connect();
//
//            int status = urlConnection.getResponseCode();
//
//            if (status != HttpURLConnection.HTTP_OK) {
//                return false;
//            }
//
//
//            int bytetotal = (int) urlConnection.getContentLength();
//            int bytesum = 0;
//            int byteread = 0;
//            out = new BufferedOutputStream(new FileOutputStream(file));
//            in = new BufferedInputStream(urlConnection.getInputStream());
//
//            final Thread thread = Thread.currentThread();
//            byte[] buffer = new byte[1444];
//            while ((byteread = in.read(buffer)) != -1) {
//                if (thread.isInterrupted()) {
//                    file.delete();
//                    throw new InterruptedIOException();
//                }
//
//                bytesum += byteread;
//                out.write(buffer, 0, byteread);
//                if (downloadListener != null && bytetotal > 0) {
//                    downloadListener.pushProgress(bytesum, bytetotal);
//                }
//            }
//            if (downloadListener != null) {
//                downloadListener.completed();
//            }
//            return true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//           in.close();
//           out.close();
//            if (urlConnection != null)
//                urlConnection.disconnect();
//        }
//
//        return false;
//    }

    private static String getBoundry() {
        StringBuffer _sb = new StringBuffer();
        for (int t = 1; t < 12; t++) {
            long time = System.currentTimeMillis() + t;
            if (time % 3 == 0) {
                _sb.append((char) time % 9);
            } else if (time % 3 == 1) {
                _sb.append((char) (65 + time % 26));
            } else {
                _sb.append((char) (97 + time % 26));
            }
        }
        return _sb.toString();
    }

    private String getBoundaryMessage(String boundary, Map params, String fileField, String fileName, String fileType) {
        StringBuffer res = new StringBuffer("--").append(boundary).append("\r\n");
        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = (String) params.get(key);
            res.append("Content-Disposition: form-data; name=\"")
                    .append(key).append("\"\r\n").append("\r\n")
                    .append(value).append("\r\n").append("--")
                    .append(boundary).append("\r\n");
        }
        res.append("Content-Disposition: form-data; name=\"").append(fileField)
                .append("\"; filename=\"").append(fileName)
                .append("\"\r\n").append("Content-Type: ")
                .append(fileType).append("\r\n\r\n");

        return res.toString();
    }

    public boolean doUploadFile(String urlStr, Map<String, String> param, String path, 
    		String imageParamName/*, final FileUploaderHttpHelper.ProgressListener listener*/){
        String BOUNDARYSTR = getBoundry();

        File targetFile = new File(path);

        byte[] barry = null;
        int contentLength = 0;
        String sendStr = "";
        try {
            barry = ("--" + BOUNDARYSTR + "--\r\n").getBytes("UTF-8");

            sendStr = getBoundaryMessage(BOUNDARYSTR, param, imageParamName, new File(path).getName(), "image/png");
            contentLength = sendStr.getBytes("UTF-8").length + (int) targetFile.length() + 2 * barry.length;
        } catch (UnsupportedEncodingException e) {
        }
        int totalSent = 0;
        String lenstr = Integer.toString(contentLength);

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        FileInputStream fis = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setConnectTimeout(UPLOAD_CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(UPLOAD_READ_TIMEOUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARYSTR);
            urlConnection.setRequestProperty("Content-Length", lenstr);
            ((HttpURLConnection) urlConnection).setFixedLengthStreamingMode(contentLength);
            urlConnection.connect();

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(sendStr.getBytes("UTF-8"));
            totalSent += sendStr.getBytes("UTF-8").length;

            fis = new FileInputStream(targetFile);

            int bytesRead;
            int bytesAvailable;
            int bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024;

            bytesAvailable = fis.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fis.read(buffer, 0, bufferSize);
            long transferred = 0;
            final Thread thread = Thread.currentThread();
            while (bytesRead > 0) {

                if (thread.isInterrupted()) {
                    targetFile.delete();
                    throw new InterruptedIOException();
                }
                out.write(buffer, 0, bufferSize);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
                transferred += bytesRead;
                if (transferred % 50 == 0)
                    out.flush();
//                if (listener != null){
//                    listener.transferred(transferred);
//                }

            }
            out.write(barry);
            totalSent += barry.length;
            out.write(barry);
            totalSent += barry.length;
            out.flush();
            out.close();
//            if (listener != null) {
//                listener.waitServerResponse();
//            }
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
				fis.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return true;
    }
    
	public static String encodeUrl(Map<String, String> param) {
		if (param == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = param.keySet();
		boolean first = true;
		for (String key : keys) {
			String value = param.get(key);
			// pain...EditMyProfileDao params' values can be empty
			if (!TextUtils.isEmpty(value) || key.equals("description") || key.equals("url")) {
				if (first) {
					first = false;
				} else {
					sb.append("&");
				}
				try {
					sb.append(URLEncoder.encode(key, "UTF-8")).append("=")
						.append(URLEncoder.encode(param.get(key), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
			}
		}
		return sb.toString();
	}

}




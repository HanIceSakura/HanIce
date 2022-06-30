package hanice.hanice;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * @author HanIce
 * @date 2022/2/25 13:01
 */
@SpringBootTest
public class PDFUtils {
    public static String splitPdf(int pageNum, String source, String dest) {
        File indexFile = new File(source);
        File outFile = new File(dest);
        PDDocument document = null;
        try {
            document = PDDocument.load(indexFile);
            Splitter splitter = new Splitter();
            splitter.setStartPage(pageNum);
            splitter.setEndPage(pageNum);
            List<PDDocument> pages = splitter.split(document);
            for (PDDocument pd : pages) {
                if (outFile.exists()) {
                    outFile.delete();
                }
                pd.save(outFile);
                pd.close();
                if (outFile.exists()) {
                    return outFile.getPath();
                }
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void pdfFileToImage(File pdffile, String targetPath) {
        try {
            FileInputStream instream = new FileInputStream(pdffile);
            InputStream byteInputStream = null;
            try {
                PDDocument doc = PDDocument.load(instream);
                PDFRenderer renderer = new PDFRenderer(doc);
                int pageCount = doc.getNumberOfPages();
                if (pageCount > 0) {
                    BufferedImage image = renderer.renderImage(0, 4.0f);
                    image.flush();
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    ImageOutputStream imOut;
                    imOut = ImageIO.createImageOutputStream(bs);
                    ImageIO.write(image, "png", imOut);
                    byteInputStream = new ByteArrayInputStream(bs.toByteArray());
                    byteInputStream.close();
                }
                doc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File uploadFile = new File(targetPath);
            FileOutputStream fops;
            fops = new FileOutputStream(uploadFile);
            fops.write(readInputStream(byteInputStream));
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 图片截取
     *
     * @param source     原始图片路径或文件流
     * @param outputPath 文件输出路径
     * @param x          开始坐标x
     * @param y          开始左边y
     * @param width      截取宽度
     * @param height     截取高度
     */
    public static String readUsingImageReader(InputStream source, String outputPath, int x, int y, int width, int height)
            throws Exception {

        // 取得图片读入器
        Iterator readers = ImageIO.getImageReadersByFormatName("png");

        ImageReader reader = (ImageReader) readers.next();

        ImageInputStream iis = ImageIO.createImageInputStream(source);
        reader.setInput(iis, true);


        // 图片参数
        ImageReadParam param = reader.getDefaultReadParam();
        try {
            Rectangle rect = new Rectangle(x, y, width, height);
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0, param);
            ImageIO.write(bi, "png", new File(outputPath));
        } finally {
            iis.close();
        }
        return outputPath;
    }

    public static String readUsingImageReader(String path, String outputPath, int x, int y, int width, int height)
            throws Exception {
        // 取得图片读入流
        try (InputStream source = new FileInputStream(path)) {
            readUsingImageReader(source, outputPath, x, y, width, height);
        }
        return outputPath;
    }

    //static String source = "F:\\工作文档\\流式\\免疫力防癌图片\\0629-39项\\";
    static String source = "F:\\工作文档\\流式\\免疫力防癌图片\\39项\\";
    static String dest = "F:\\工作文档\\流式\\免疫力防癌图片\\outs\\";
    static String imgs = "F:\\工作文档\\流式\\免疫力防癌图片\\imgs\\";

    public static void main(String source, String dest, String imgs) throws Exception {
        String path = splitPdf(2, source, dest);
        File file = new File(path);
        //上传的是png格式的图片结尾
        pdfFileToImage(file, imgs + ".png");
        intercept(imgs);
    }

    public static void intercept(String imgs) throws Exception {
        readUsingImageReader(imgs + ".png", imgs + ".jpg", 0, 0, 2500, 3200);
        //输出pdf图片
        if (new File(imgs).exists()) {
            new File(imgs).delete();
        }
    }

    public static void main(String[] args) throws Exception {
        String path = source;        //要遍历的路径
        String name = null;
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory()) {       //若非目录(即文件)，则打印
                name = f.toString().substring(23, f.toString().length() - 4);
                main(f.toString(), dest + "out" + name + ".pdf", imgs + name); //
                System.out.println(f);
            }
        }
    }
}

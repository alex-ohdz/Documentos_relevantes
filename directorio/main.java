import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


public class main {
    public static void index(String xmlDir, String indexDir, String schemaFile) throws Exception {
        // Crear objeto SAXBuilder para leer los documentos XML
        SAXBuilder builder = new SAXBuilder();

        // Crear objeto IndexWriter para agregar documentos al índice
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        //crear indice de busqueda
        IndexWriter writer = new IndexWriter(dir, iwc);

        // Crear objeto Validator a partir del archivo XSD
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File schemaFile1 = new File(schemaFile);
        Schema schema = schemaFactory.newSchema(schemaFile1);
        Validator validator = schema.newValidator();

        // Recorrer los documentos XML y agregarlos al índice
        File xmlFolder = new File(xmlDir);
        File[] xmlFiles = xmlFolder.listFiles();
        for (File xmlFile : xmlFiles) {
            // Validar el documento XML contra el XSD
            validator.validate(new StreamSource(xmlFile));

            // Crear objeto JDom a partir del documento XML
            org.jdom2.Document jdomDoc = builder.build(xmlFile);

            // Recorrer las Unidades Estructurales y agregar un campo al índice por cada una

            Element root = jdomDoc.getRootElement();
            List<Element> ues = root.getChildren();
            Document luceneDoc = new Document();
            for (Element ue : ues) {
                String ueName = ue.getName();
                String ueValue = ue.getText();
                //System.out.println(ueName + " " + ueValue);
                Field ueField = new TextField(ueName, ueValue, Field.Store.YES);
                luceneDoc.add(ueField);
            }

            // Agregar el documento al índice
            writer.addDocument(luceneDoc);
        }


        // Cerrar el IndexWriter y el directorio
        writer.close();
        dir.close();
    }

    public static void main(String[] args) {
        String indexSrc = "src/indexDB";
        String xmlSrc = "src/xmlDB";
        try {
            index(xmlSrc, indexSrc, "src/schema/mainSchema.xsd");
            String query = "Facultad:MFC";

            Directory indexDirectory = FSDirectory.open(Paths.get(indexSrc));
            IndexReader reader = DirectoryReader.open(indexDirectory);
            IndexSearcher indexSearcher = new IndexSearcher(reader);

            QueryParser queryParser = new QueryParser("contents", new StandardAnalyzer());
            Query response = queryParser.parse(query);

            TopDocs doc = indexSearcher.search(response, reader.numDocs());
            System.out.println(doc.totalHits.value + " documentos encontrados.");

                for (ScoreDoc scoredoc : doc.scoreDocs) {
                    System.out.println("Puntuación: " + scoredoc.score);

                }
            } catch (IOException e) {
                System.out.println("Hubo un error al acceder al directorio de índice o de XML: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Hubo un error al realizar la búsqueda: " + e.getMessage());
            }
                }

            }



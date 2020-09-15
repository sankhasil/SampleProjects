FROM python:3.8 AS intermediate

ARG PIP_EXTRA_INDEX_URL

COPY src/ /aiqcheck-package/src
COPY .git/ aiqcheck-package/.git
COPY setup.cfg setup.py /aiqcheck-package/

RUN pip install /aiqcheck-package'[service]'

RUN chgrp -R root /usr/local

FROM python:3.8
LABEL maintainer="sankha.sil@ai4bd.com"

RUN useradd -M aiqcheck

COPY --from=intermediate /usr/local/lib/python3.8/site-packages/ \
     /usr/local/lib/python3.8/site-packages/

COPY --from=intermediate --chown=aiqcheck:aiqcheck /aiqcheck-package /aiqcheck-package

RUN apt update && apt upgrade -y && apt install openjdk-11-jre -y --no-install-recommends
RUN pip install gunicorn==19.9

USER aiqcheck

CMD gunicorn aiqcheck.service.app:APP.wsgi_app --bind 0.0.0.0:8000
